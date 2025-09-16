package snapmeal.snapmeal.service;

import com.amazonaws.services.kms.model.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.Arrays;
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

    @Transactional
    public ChallengeDto.ParticipateResponse participate(Long challengeId) {
        User user = authService.getCurrentUser();
        Challenges c = challengeRepository.findByChallengeIdAndUser(challengeId, user)
                .orElseThrow(() -> new NotFoundException("챌린지를 찾을 수 없습니다."));
        c.participate();
        return new ChallengeDto.ParticipateResponse(c.getStatus().name());
    }

    @Transactional
    public ChallengeDto.ParticipateResponse giveUp(Long challengeId) {
        User user = authService.getCurrentUser();
        Challenges c = challengeRepository.findByChallengeIdAndUser(challengeId, user)
                .orElseThrow(() -> new NotFoundException("챌린지를 찾을 수 없습니다."));
        c.giveUp();
        return new ChallengeDto.ParticipateResponse(c.getStatus().name());
    }

    @Transactional(readOnly = true)
    public List<Challenges> listMine(String statusesCsv) {
        User user = authService.getCurrentUser();
        List<ChallengeStatus> statuses = Arrays.stream(statusesCsv.split(","))
                .map(String::trim)
                .map(ChallengeStatus::valueOf)
                .toList();
        return challengeRepository.findAllByUserAndStatusIn(user, statuses);
    }

    /** 매일 00:10에 어제까지 종료된 챌린지를 SUCCESS/FAIL로 판정 */
    @Transactional
    public void evaluateDaily() {
        LocalDate today = LocalDate.now(KST);
        List<Challenges> targets = challengeRepository.findAllByStatusInAndEndDateLessThanEqual(
                List.of(ChallengeStatus.PENDING, ChallengeStatus.IN_PROGRESS),
                today.minusDays(1)
        );

        for (Challenges c : targets) {
            LocalDateTime startDt = c.getStartDate().atStartOfDay();
            LocalDateTime endDt = c.getEndDate().atTime(23, 59, 59);

            boolean ok = mealsRepository.existsByUserAndMealDateBetweenAndMenuContainingIgnoreCase(
                    c.getUser(), startDt, endDt, c.getTargetMenuName()
            );
            if (ok) c.success(LocalDateTime.now(KST));
            else c.fail();
        }
    }

    @Transactional
    public ChallengeDto.ReviewResponse createReview(Long challengeId, ChallengeDto.ReviewCreateOrUpdateRequest req) {
        if (req.getRating() == null || req.getRating() < 1 || req.getRating() > 5) {
            throw new IllegalArgumentException("별점은 1~5 범위여야 합니다.");
        }
        User user = authService.getCurrentUser();
        Challenges challenge = challengeRepository.findByChallengeIdAndUser(challengeId, user)
                .orElseThrow(() -> new NotFoundException("챌린지를 찾을 수 없습니다."));

        ChallengeReviews review = reviewRepository.save(
                ChallengeReviews.builder()
                        .challenge(challenge)
                        .user(user)
                        .rating(req.getRating())
                        .content(req.getContent())
                        .build()
        );
        return toReviewDto(review);
    }

    @Transactional
    public ChallengeDto.ReviewResponse updateReview(Long reviewId, ChallengeDto.ReviewCreateOrUpdateRequest req) {
        User user = authService.getCurrentUser();
        ChallengeReviews review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("후기를 찾을 수 없습니다."));
        if (!review.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("본인 후기만 수정할 수 있습니다.");
        }
        if (req.getRating() != null) {
            if (req.getRating() < 1 || req.getRating() > 5) {
                throw new IllegalArgumentException("별점은 1~5 범위여야 합니다.");
            }
            review.setRating(req.getRating());
        }
        review.setContent(req.getContent());
        return toReviewDto(review);
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        User user = authService.getCurrentUser();
        ChallengeReviews review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("후기를 찾을 수 없습니다."));
        if (!review.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("본인 후기만 삭제할 수 있습니다.");
        }
        reviewRepository.delete(review);
    }

    private ChallengeDto.ReviewResponse toReviewDto(ChallengeReviews r) {
        ChallengeDto.ReviewResponse dto = new ChallengeDto.ReviewResponse();
        dto.setReviewId(r.getReviewId());
        dto.setChallengeId(r.getChallenge().getChallengeId());
        dto.setRating(r.getRating());
        dto.setContent(r.getContent());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setUpdatedAt(r.getUpdatedAt());
        return dto;
    }
}
