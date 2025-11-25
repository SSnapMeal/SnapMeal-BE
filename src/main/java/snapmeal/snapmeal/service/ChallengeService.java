package snapmeal.snapmeal.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import snapmeal.snapmeal.converter.ChallengeConverter;
import snapmeal.snapmeal.domain.ChallengeReviews;
import snapmeal.snapmeal.domain.Challenges;
import snapmeal.snapmeal.domain.User;
import snapmeal.snapmeal.domain.enums.ChallengeStatus;
import snapmeal.snapmeal.global.util.AuthService;
import snapmeal.snapmeal.repository.ChallengeRepository;
import snapmeal.snapmeal.repository.ChallengeReviewRepository;
import snapmeal.snapmeal.repository.MealsRepository;
import snapmeal.snapmeal.web.dto.ChallengeDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final ChallengeReviewRepository reviewRepository;
    private final MealsRepository mealsRepository;
    private final AuthService authService;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    /* ==========================
     * 스탬프/판정 유틸
     * ========================== */

    // (임시) 회피형 여부. 엔티티 컬럼 도입 전까지 사용.
    private boolean isAvoidType(Challenges c) {
        String t = (c.getTitle() + " " + c.getDescription()).toLowerCase();
        return t.contains("안마시") || t.contains("금지") || t.contains("끊") || t.contains("avoid");
    }

    // 특정 날짜 d에서 "그 날 규칙 충족 여부" 계산
    private boolean isSatisfiedOn(Challenges c, LocalDate d) {
        LocalDateTime begin = d.atStartOfDay();
        LocalDateTime end   = d.atTime(23, 59, 59);

        boolean contains = mealsRepository
                .existsByUserAndMealDateBetweenAndMenuContainingIgnoreCase(
                        c.getUser(), begin, end, c.getTargetMenuName()
                );

        // 회피형: 해당 메뉴가 없어야 충족 / 섭취형: 있으면 충족
        return isAvoidType(c) ? !contains : contains;
    }

    // 시작~종료 기간 동안 일별 스탬프(boolean[]) 계산
    private boolean[] buildDailyStamps(Challenges c) {
        LocalDate start = c.getStartDate();
        LocalDate end   = c.getEndDate();

        int days = (int) (end.toEpochDay() - start.toEpochDay()) + 1;
        boolean[] stamps = new boolean[days];

        LocalDate today = LocalDate.now(KST);
        for (int i = 0; i < days; i++) {
            LocalDate d = start.plusDays(i);
            // 미래 날짜는 미판정 → false 유지 (원하면 DTO를 Boolean[]로 바꿔 null 처리)
            if (!d.isAfter(today)) {
                stamps[i] = isSatisfiedOn(c, d);
            }
        }
        return stamps;
    }

    private boolean hasChallengeEnded(Challenges c) {
        // endDate < 내일 == 이미 종료
        return c.getEndDate().isBefore(LocalDate.now(KST).plusDays(1));
    }

    /* ==========================
     * 도메인 기능
     * ========================== */

    // 참여하기
    @Transactional
    public ChallengeDto.Response.ParticipateResponse participate(Long challengeId) {
        User user = authService.getCurrentUser();
        Challenges c = challengeRepository.findByChallengeIdAndUser(challengeId, user)
                .orElseThrow(() -> new EntityNotFoundException("챌린지를 찾을 수 없습니다."));
        c.participate();
        return new ChallengeDto.Response.ParticipateResponse(c.getStatus().name());
    }

    // 포기하기
    @Transactional
    public ChallengeDto.Response.ParticipateResponse giveUp(Long challengeId) {
        User user = authService.getCurrentUser();
        Challenges c = challengeRepository.findByChallengeIdAndUser(challengeId, user)
                .orElseThrow(() -> new EntityNotFoundException("챌린지를 찾을 수 없습니다."));
        c.giveUp();
        return new ChallengeDto.Response.ParticipateResponse(c.getStatus().name());
    }

    // 내 챌린지 목록
    @Transactional(readOnly = true)
    public List<Challenges> listMine(String statusesCsv) {
        User user = authService.getCurrentUser();
        List<ChallengeStatus> statuses = Arrays.stream(statusesCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(ChallengeStatus::valueOf)
                .toList();
        return challengeRepository.findAllByUserAndStatusIn(user, statuses);
    }

    // 스탬프 찍기
    @Transactional(readOnly = true)
    public List<ChallengeDto.Response> listMineWithStamps(String statusesCsv) {
        User user = authService.getCurrentUser();
        List<ChallengeStatus> statuses = Arrays.stream(statusesCsv.split(","))
                .map(String::trim).filter(s -> !s.isEmpty())
                .map(ChallengeStatus::valueOf).toList();

        var challenges = challengeRepository.findAllByUserAndStatusIn(user, statuses);

        List<ChallengeDto.Response> result = new ArrayList<>();
        for (Challenges c : challenges) {
            // 기본 상세 DTO
            ChallengeDto.Response dto = ChallengeConverter.toDetailDto(c);

            // 스탬프/충족일수 채우기 (getDetail과 동일 로직)
            boolean[] stamps = buildDailyStamps(c);
            dto.setStamps(stamps);
            int satisfied = 0; for (boolean s : stamps) if (s) satisfied++;
            dto.setSatisfiedDays(satisfied);

            result.add(dto);
        }
        return result;
    }
    /**
     * 참여 전, 내 계정에 "생성되어 있는" 전체 챌린지 조회
     * - 정의: 보통 PENDING(후보) 상태 = 아직 참여/포기/성공/실패 결정 전
     * - 정렬: 시작일 최신순 (최근에 생성된 후보가 위로 오도록)
     * - 반환: 목록 화면용 경량 DTO (toDto)
     */
    @Transactional(readOnly = true)
    public List<ChallengeDto.Response> listAvailableAll() {
        User user = authService.getCurrentUser();

        // 기존 findAllByUserAndStatusIn 재사용 (레포 메서드 추가 불필요)
        var entities = challengeRepository.findAllByUserAndStatusIn(
                user, List.of(ChallengeStatus.PENDING)
        );

        return entities.stream()
                .sorted(Comparator.comparing(Challenges::getStartDate).reversed())
                .map(ChallengeConverter::toDto)
                .toList();
    }

    // 참여 중인 챌린지(IN_PROGRESS)만 스탬프 포함해 반환
    @Transactional(readOnly = true)
    public List<ChallengeDto.Response> listParticipatingWithStamps() {
        User user = authService.getCurrentUser();

        // IN_PROGRESS만 조회
        var challenges = challengeRepository.findAllByUserAndStatusIn(
                user, List.of(ChallengeStatus.IN_PROGRESS)
        );

        List<ChallengeDto.Response> result = new ArrayList<>();
        for (Challenges c : challenges) {
            ChallengeDto.Response dto = ChallengeConverter.toDetailDto(c);

            // 참여 중일 때만 스탬프 계산/세팅
            boolean[] stamps = buildDailyStamps(c);
            dto.setStamps(stamps);
            int satisfied = 0; for (boolean s : stamps) if (s) satisfied++;
            dto.setSatisfiedDays(satisfied);

            result.add(dto);
        }
        return result;
    }

    /**
     * 챌린지 상세조회
     * - 로그인한 사용자의 챌린지 중에서 challengeId로 찾음
     * - 응답 DTO에 일별 스탬프/충족일수 포함
     */
    @Transactional(readOnly = true)
    public ChallengeDto.Response getDetail(Long challengeId, Long currentUserId) {
        User currentUser = authService.getCurrentUser();
        Challenges c = challengeRepository.findByChallengeIdAndUser(challengeId, currentUser)
                .orElseThrow(() -> new EntityNotFoundException("해당 챌린지를 찾을 수 없습니다."));

        ChallengeDto.Response dto = ChallengeConverter.toDetailDto(c);

        // 참여 중(IN_PROGRESS)일 때만 스탬프 계산/세팅
        if (c.getStatus() != ChallengeStatus.PENDING) {
            boolean[] stamps = buildDailyStamps(c);
            dto.setStamps(stamps);
            int satisfied = 0; for (boolean s : stamps) if (s) satisfied++;
            dto.setSatisfiedDays(satisfied);
        } else {
            // 스탬프 숨김(원하면 null 세팅)
            dto.setStamps(null);
            dto.setSatisfiedDays(0);
        }

        return dto;
    }

    /**
     * 종료 챌린지 판정 (스케줄 호출은 ChallengeScheduler에서)
     * - 회피형: 모든 날 충족 → SUCCESS
     * - 섭취형: 1일 이상 충족 → SUCCESS   (필요 시 요구치 컬럼과 비교)
     */
    @Transactional
    public void evaluateDaily() {
        LocalDate today = LocalDate.now(KST);

        // PENDING + IN_PROGRESS 중 어제까지 끝난 챌린지만 판정 대상
        List<Challenges> targets = challengeRepository
                .findAllByStatusInAndEndDateLessThanEqual(
                        List.of(ChallengeStatus.PENDING, ChallengeStatus.IN_PROGRESS),
                        today.minusDays(1)  // 어제까지 끝난 챌린지
                );

        for (Challenges c : targets) {

            // 끝날 때까지 참여 안 한 챌린지 -> NOT_PARTICIPATED
            if (c.getStatus() == ChallengeStatus.PENDING) {
                c.markNotParticipated();
                continue;
            }

            // 여기 오면 무조건 IN_PROGRESS -> 성공/실패 판정
            boolean[] stamps = buildDailyStamps(c);
            int satisfied = 0;
            for (boolean s : stamps) if (s) satisfied++;

            boolean success = isAvoidType(c)
                    ? (satisfied == stamps.length)  // 회피형: 전부 만족했을 때만 성공
                    : (satisfied >= 1);             // 섭취형: 1일이라도 만족하면 성공

            if (success) {
                c.success(LocalDateTime.now(KST)); // SUCCESS + 종료시간 기록
            } else {
                c.fail(); // FAIL
            }
        }
    }

    /* ==========================
     * 리뷰 (0~5점, 종료 후만)
     * ========================== */

    @Transactional
    public ChallengeDto.Response.ReviewResponse createReview(
            Long challengeId,
            ChallengeDto.Response.ReviewCreateOrUpdateRequest req
    ) {
        // 0~5 허용
        if (req.getRating() == null || req.getRating() < 0 || req.getRating() > 5) {
            throw new IllegalArgumentException("별점은 0~5 범위여야 합니다.");
        }
        User user = authService.getCurrentUser();
        Challenges c = challengeRepository.findByChallengeIdAndUser(challengeId, user)
                .orElseThrow(() -> new EntityNotFoundException("챌린지를 찾을 수 없습니다."));

        // 종료 후만 작성 가능
        if (!hasChallengeEnded(c)) {
            throw new IllegalStateException("챌린지 종료 후에만 리뷰를 작성할 수 있습니다.");
        }
        // 포기하거나 미참여 챌린지 리뷰 작성 불가능
        if (c.getStatus() == ChallengeStatus.NOT_PARTICIPATED
                || c.getStatus() == ChallengeStatus.CANCELLED) {
            throw new IllegalStateException("참여 중이거나 완료된 챌린지만 리뷰를 작성할 수 있습니다.");
        }

        ChallengeReviews review = reviewRepository.save(
                ChallengeReviews.builder()
                        .challenge(c)
                        .user(user)
                        .rating(req.getRating())
                        .content(req.getContent())
                        .build()
        );
        return toReviewDto(review);
    }

    // 별점 및 후기 작성
    @Transactional
    public ChallengeDto.Response.ReviewResponse updateReview(
            Long reviewId,
            ChallengeDto.Response.ReviewCreateOrUpdateRequest req
    ) {
        User user = authService.getCurrentUser();
        ChallengeReviews review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("후기를 찾을 수 없습니다."));
        if (!review.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("본인 후기만 수정할 수 있습니다.");
        }
        if (req.getRating() != null) {
            if (req.getRating() < 0 || req.getRating() > 5) {
                throw new IllegalArgumentException("별점은 0~5 범위여야 합니다.");
            }
            review.setRating(req.getRating());
        }
        if (req.getContent() != null) {
            review.setContent(req.getContent());
        }
        return toReviewDto(review);
    }

    // 후기 삭제
    @Transactional
    public void deleteReview(Long reviewId) {
        User user = authService.getCurrentUser();
        ChallengeReviews review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("후기를 찾을 수 없습니다."));
        if (!review.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("본인 후기만 삭제할 수 있습니다.");
        }
        reviewRepository.delete(review);
    }

    private ChallengeDto.Response.ReviewResponse toReviewDto(ChallengeReviews r) {
        ChallengeDto.Response.ReviewResponse dto = new ChallengeDto.Response.ReviewResponse();
        dto.setReviewId(r.getReviewId());
        dto.setChallengeId(r.getChallenge().getChallengeId());
        dto.setRating(r.getRating());
        dto.setContent(r.getContent());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setUpdatedAt(r.getUpdatedAt());
        return dto;
    }

    // ChallengeService.java

    @Transactional(readOnly = true)
    public List<ChallengeDto.Response> listCurrentGeneratedChallenges() {
        User user = authService.getCurrentUser();

        // 현재 사용자에게 생성된 모든 챌린지를 최신순으로 조회
        List<Challenges> challenges = challengeRepository
                .findAllByUserOrderByStartDateDesc(user);

        // 가벼운 DTO로 변환 (스탬프 불필요)
        return challenges.stream()
                .map(ChallengeConverter::toDto)
                .toList();
    }
    /**
     * 특정 챌린지에 작성된 리뷰 단건 조회
     *  - 챌린지 당 1개의 리뷰만 존재
     *  - 없으면 null 혹은 예외 처리(필요에 따라 선택)
     */
    @Transactional(readOnly = true)
    public ChallengeDto.Response.ReviewResponse getReview(Long challengeId) {
        User user = authService.getCurrentUser();

        // 챌린지가 내 소유인지 확인
        Challenges challenge = challengeRepository
                .findByChallengeIdAndUser(challengeId, user)
                .orElseThrow(() -> new EntityNotFoundException("챌린지를 찾을 수 없습니다."));

        // 리뷰 단건 조회
        ChallengeReviews review = reviewRepository
                .findByChallenge(challenge)
                .orElse(null);  // 리뷰가 없을 때 -> null 반환

        // DTO 변환 (null 처리 가능)
        return (review != null) ? toReviewDto(review) : null;
    }

    // 내가 작성한 챌린지 리뷰 전체 조회
    @Transactional(readOnly = true)
    public List<ChallengeDto.Response.ReviewResponse> listMyReviews() {
        User user = authService.getCurrentUser();

        // 내가 쓴 리뷰들 최신순 조회
        List<ChallengeReviews> reviews =
                reviewRepository.findAllByUserOrderByCreatedAtDesc(user);

        // DTO로 변환
        return reviews.stream()
                .map(this::toReviewDto)
                .toList();
    }

    /**
     * ⚠️ 테스트용 리뷰 생성 (기간/상태 제한 없이 강제 생성)
     * 실제 운영에서는 사용 X
     */
    @Transactional
    public ChallengeDto.Response.ReviewResponse createReviewForTest(
            Long challengeId,
            ChallengeDto.Response.ReviewCreateOrUpdateRequest req
    ) {
        // 0~5 허용
        if (req.getRating() == null || req.getRating() < 0 || req.getRating() > 5) {
            throw new IllegalArgumentException("별점은 0~5 범위여야 합니다.");
        }

        User user = authService.getCurrentUser();
        Challenges c = challengeRepository.findByChallengeIdAndUser(challengeId, user)
                .orElseThrow(() -> new EntityNotFoundException("챌린지를 찾을 수 없습니다."));

        // 기간/상태 무시하고 리뷰 바로 저장 (테스트용)
        ChallengeReviews review = reviewRepository.save(
                ChallengeReviews.builder()
                        .challenge(c)
                        .user(user)
                        .rating(req.getRating())
                        .content(req.getContent())
                        .build()
        );

        return toReviewDto(review);
    }
}