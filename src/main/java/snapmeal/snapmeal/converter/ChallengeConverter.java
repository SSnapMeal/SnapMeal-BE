package snapmeal.snapmeal.converter;

import org.springframework.stereotype.Component;
import snapmeal.snapmeal.domain.Challenges;
import snapmeal.snapmeal.domain.enums.ChallengeStatus;
import snapmeal.snapmeal.web.dto.ChallengeDto;

import java.time.LocalDateTime;

@Component
public class ChallengeConverter {
   /**
    * [리스트용] 가벼운 응답: 목록 카드 등에 사용
    * - participation/introduction 같은 중첩 데이터는 제외
    */
   public static ChallengeDto.Response toListDto(Challenges c) {
       return ChallengeDto.Response.builder()
               .challengeId(c.getChallengeId())
               .title(c.getTitle())
               .coverImageUrl(resolveCoverImageUrl(c)) // 엔티티에 없으면 규칙으로 생성
               .targetMenuName(c.getTargetMenuName())
               .description(c.getDescription())
               .status(c.getStatus().name())
               .startDate(c.getStartDate())
               .endDate(c.getEndDate())
               // 리스트에서는 introduction/participation 생략(필요시 최소 정보만 넣어도 됨)
               // ✅ 회피형 여부 노출
               .isAvoidType(c.isAvoidType())
               .build();
   }
    /**
     * [상세용] 사진 구조 그대로 채우는 응답:
     * - introduction (mainGoal, purpose, detailDescription, weeklyTarget, successCondition)
     * - participation (isParticipating, participatedAt)
     */
    public static ChallengeDto.Response toDetailDto(Challenges c) {
        // 참여 여부 정책: IN_PROGRESS면 참여 중(필요 시 정책 바꿔도 됨)
        boolean isParticipating = c.getStatus() == ChallengeStatus.IN_PROGRESS;
        LocalDateTime participatedAt = c.getParticipatedAt();

        return ChallengeDto.Response.builder()
                .challengeId(c.getChallengeId())
                .title(c.getTitle())
                .coverImageUrl(resolveCoverImageUrl(c))
                .targetMenuName(c.getTargetMenuName())
                .description(c.getDescription())
                .status(c.getStatus().name())
                .startDate(c.getStartDate())
                .endDate(c.getEndDate())
                // ✅ 회피형 여부
                .isAvoidType(c.isAvoidType())
                // 소개 섹션: 엔티티에 전용 칼럼이 없으면 규칙/상수로 보강
                .introduction(ChallengeDto.Response.Introduction.builder()
                        .mainGoal(buildMainGoal(c))                        // 예: "커피 안마시기" 또는 "사과 먹기"
                        .purpose(buildPurpose(c))                          // ✅ 동적 목적
                        .detailDescription(c.getDescription())             // 엔티티 설명 그대로
                        .weeklyTarget(c.isAvoidType() ? "매일 금지 도전" : "기간 중 1일 이상 기록") // ✅ 정책 반영
                        .successCondition(buildSuccessCondition(c))        // ✅ 동적 성공조건
                        .build())
                .participation(ChallengeDto.Response.ParticipationInfo.builder()
                        .isParticipating(c.getStatus() == ChallengeStatus.IN_PROGRESS) // ✅ 필드명 변경 반영
                        .participatedAt(c.getParticipatedAt())
                        .build())

                .build();
    }
    // 목록용 (필요한 정보만)
    public static ChallengeDto.Response toDto(Challenges c) {
        return ChallengeDto.Response.builder()
                .challengeId(c.getChallengeId())
                .title(c.getTitle())
                .targetMenuName(c.getTargetMenuName())
                .description(c.getDescription())
                .startDate(c.getStartDate())
                .endDate(c.getEndDate())
                .status(c.getStatus().name())
                .build();
    }

    // ────────────────────────── 헬퍼들 ──────────────────────────

    /** coverImageUrl 우선순위: 엔티티 값 > 규칙 생성 */
    private static String resolveCoverImageUrl(Challenges c) {
        // 엔티티에 URL이 없으면 targetMenuName 기반으로 자동 생성
        String key = (c.getTargetMenuName() == null)
                ? "default"
                : c.getTargetMenuName().trim().toLowerCase();

        // 한글이면 CDN 규칙에 따라 변환 필요할 수도 있음(예: 커피 → coffee)
        return "https://cdn.snapmeal.app/challenges/" + key + "-off.png";
    }

    /** 한글/공백 등을 CDN 경로용으로 간단히 슬러그화(필요시 더 견고한 변환 로직 적용) */
    private static String slugify(String s) {
        // 예시: 소문자 변환 + 공백/특수문자 -> 하이픈
        return s.trim().toLowerCase()
                .replaceAll("[\\s]+", "-")
                .replaceAll("[^a-z0-9\\-]", ""); // 한글이면 CDN 규칙에 맞게 별도 매핑 필요
    }

    /** 타이틀에서 간단 규칙으로 mainGoal 생성 (엔티티 칼럼이 있으면 그거 쓰는 게 가장 좋음) */
    private static String buildMainGoal(Challenges c) {
        String title = c.getTitle() == null ? "" : c.getTitle();
        if (title.endsWith(" 마시기")) {
            return title.replace(" 마시기", " 안마시기");
        }
        return title; // 기본은 타이틀 재사용
    }

    private static String buildPurpose(Challenges c) {
        // 설명이 들어있으면 그걸 목적처럼 노출
        if (c.getDescription() != null && !c.getDescription().isBlank()) {
            return c.getDescription();
        }
        return c.isAvoidType()
                ? "섭취 빈도 줄이기 및 절제 습관 형성"
                : "영양 보충 및 건강한 식습관 형성";
    }

    private static String buildSuccessCondition(Challenges c) {
        long days = c.getEndDate().toEpochDay() - c.getStartDate().toEpochDay() + 1;
        if (c.isAvoidType()) {
            return String.format("기간(%d일) 동안 \"%s\" 미기록 시 성공", days, c.getTargetMenuName());
        } else {
            // 현재 로직: 섭취형은 기간 중 1일 이상 기록하면 성공
            return String.format("기간(%d일) 동안 \"%s\" 기록 1일 이상 시 성공", days, c.getTargetMenuName());
        }
    }

}
