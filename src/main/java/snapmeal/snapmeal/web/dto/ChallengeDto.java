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

        @Schema(example = "커피")
        private String targetMenuName;

        @Schema(example = "오늘은 아메리카노 한 잔!")
        private String description;

        @Schema(example = "2025-09-01")
        private LocalDate startDate;

        @Schema(example = "2025-09-07")
        private LocalDate endDate;

        @Schema(example = "IN_PROGRESS")
        private String status;

        private LocalDateTime participatedAt;
        private LocalDateTime completedAt;
        private LocalDateTime cancelledAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class ParticipateResponse {
        @Schema(example = "\"IN_PROGRESS\"")
        private String status;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class ReviewCreateOrUpdateRequest {
        @Schema(example = "5")
        private Integer rating;  // 1~5

        @Schema(example = "재밌었다~!")
        private String content;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
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
