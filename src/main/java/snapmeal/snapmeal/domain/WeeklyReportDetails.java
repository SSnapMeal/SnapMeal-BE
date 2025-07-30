package snapmeal.snapmeal.domain;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WeeklyReportDetails {

    private String summaryNutritionIntake;
    private String nutritionBalanceComment;
    private String timeBasedCaloriePattern;
    private String dinnerStatistics;
    private String overallNutritionEvaluation;
    private String foodSuggestion;
    private String dinnerGuidance;
    private String finalMotivationalMessage;
}

