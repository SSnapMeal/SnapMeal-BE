package snapmeal.snapmeal.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ChallengeDto {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {
        @Schema(example = "1")
        private Long challengeId; // 엔티티 PK와 매핑

        @Schema(example = "커피 마시기")
        private String title;

        private String coverImageUrl; // 상단 배너 이미지

        @Schema(example = "커피")
        private String targetMenuName;

        @Schema(example = "아메리카노, 에스프레소, 라떼 등 모든 커피 종류 포함")
        private String description;

        @Schema(example = "IN_PROGRESS")
        private String status;

        @Schema(example = "2025-09-01")
        private LocalDate startDate;

        @Schema(example = "2025-09-07")
        private LocalDate endDate;

        // 회피형 여부(엔티티 컬럼 도입 시 매핑)
        @Schema(example = "true", description = "회피형 챌린지 여부 (예: 커피 안마시기)")
        private Boolean isAvoidType;

        // 스탬프: 1일차~N일차, 각 날짜 충족 여부
        @Schema(example = "[true,false,true,false,true,false,true]", description = "1일차~N일차 일별 충족 여부")
        private boolean[] stamps;

        // 충족 일수 (UI 요약용)
        @Schema(example = "4", description = "충족한 일수 합계")
        private int satisfiedDays;

        private Introduction introduction;    // 소개 섹션
        private ParticipationInfo participation; // 참여 정보

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class Introduction {
            private String mainGoal;          // 주 목표 (예: 커피 안마시기)
            private String purpose;           // 목적 (예: 카페인 줄이기 및 건강 관리)
            private String detailDescription; // 상세설명
            private String weeklyTarget;      // 달성 목표 (예: 주 5회 이상)
            private String successCondition;  // 달성 조건 (예: 기간 동안 커피 관련 미기록시 성공)
        }

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class ParticipationInfo {
            // 참여 중이면 true -> '참여하기' 버튼 비활성화 용
            @com.fasterxml.jackson.annotation.JsonProperty("isParticipating")
            private boolean isParticipating;
            // 참여 시작 시각 (참여 안했으면 null)
            private LocalDateTime participatedAt;
        }

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ParticipateResponse {
            @Schema(example = "\"IN_PROGRESS\"")
            private String status;
        }

        /* ---------- 리뷰 DTO ---------- */

        // 요청 DTO는 "입력"에만 쓰이므로 Response 내부 보관해도 되지만,
        // 나중에 분리하고 싶으면 ChallengeReviewRequestDto로 빼면 깔끔.
        @Getter @Setter @NoArgsConstructor @AllArgsConstructor
        public static class ReviewCreateOrUpdateRequest {
            // ✅ 별점 0~5로 수정 (요구사항 반영) — 서버 검증도 0~5로 변경했는지 확인!
            @Schema(example = "4", minimum = "0", maximum = "5")
            private Integer rating;
            @Schema(example = "생각보다 힘들었지만 몸이 가벼워졌어요!")
            private String content;
        }

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ReviewResponse {
            private Long reviewId;
            private Long challengeId;

            @Schema(example = "5")
            private Integer rating;

            @Schema(example = "다음에 또 해야지!")
            private String content;

            private LocalDateTime createdAt;
            private LocalDateTime updatedAt;
        }
    }
}
