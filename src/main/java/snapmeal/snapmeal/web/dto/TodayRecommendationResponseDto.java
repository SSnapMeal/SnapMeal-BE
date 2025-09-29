package snapmeal.snapmeal.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Schema(description = "오늘의 추천 응답 DTO")
public class TodayRecommendationResponseDto {

    @Schema(description = "오늘까지 섭취한 칼로리", example = "1200")
    private int consumedCalories;

    @Schema(description = "남은 칼로리", example = "1000")
    private int remainingCalories;

    @Schema(description = "운동 추천 리스트")
    private List<ExerciseRecommendationDto> exercises;

    @Schema(description = "음식 추천 리스트")
    private List<FoodRecommendationDto> foods;

    @Getter
    @Builder
    @NoArgsConstructor(force = true)
    @AllArgsConstructor
    @Schema(description = "운동 추천 DTO")
    public static class ExerciseRecommendationDto {
        @Schema(description = "운동 이름", example = "자전거 타기")
        private String name;

        @Schema(description = "운동으로 소모되는 칼로리", example = "200")
        private int calories;

        @Schema(description = "운동 시간", example = "1시간")
        private String duration;

        @Schema(description = "반복 횟수", example = "3")
        private int repeat;

        @Schema(description = "운동 카테고리", example = "유산소")
        private String category;

        @Schema(description = "운동 이모지", example = "🚴")
        private String emoji;
    }

    @Getter
    @Builder
    @NoArgsConstructor(force = true)
    @AllArgsConstructor
    @Schema(description = "음식 추천 DTO")
    public static class FoodRecommendationDto {
        @Schema(description = "음식 이름", example = "샐러드")
        private String name;

        @Schema(description = "음식 칼로리", example = "302")
        private int calories;

        @Schema(description = "음식의 영양학적 장점", example = "부족한 영양소 채우기")
        private String benefit;

        @Schema(description = "음식 이모지", example = "🥗")
        private String emoji;
    }
}
