package snapmeal.snapmeal.web.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(force = true)
public class TodayRecommendationResponseDto {
    private int consumedCalories;
    private int remainingCalories;
    private String exerciseSuggestion;
    private String foodSuggestion;

    public TodayRecommendationResponseDto(int consumedCalories, int remainingCalories, String exerciseSuggestion, String foodSuggestion) {
        this.consumedCalories = consumedCalories;
        this.remainingCalories = remainingCalories;
        this.exerciseSuggestion = exerciseSuggestion;
        this.foodSuggestion = foodSuggestion;
    }
}
