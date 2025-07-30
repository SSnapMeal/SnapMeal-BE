package snapmeal.snapmeal.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.JSONObject;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyReportDetailsDto {

    private String summaryNutritionIntake;
    private String nutritionBalanceComment;
    private String timeBasedCaloriePattern;
    private String dinnerStatistics;
    private String overallNutritionEvaluation;
    private String foodSuggestion;
    private String dinnerGuidance;
    private String finalMotivationalMessage;

    public static WeeklyReportDetailsDto fromJson(String jsonStr) {
        JSONObject json = new JSONObject(jsonStr);
        return new WeeklyReportDetailsDto(
                json.optString("summaryNutritionIntake"),
                json.optString("nutritionBalanceComment"),
                json.optString("timeBasedCaloriePattern"),
                json.optString("dinnerStatistics"),
                json.optString("overallNutritionEvaluation"),
                json.optString("foodSuggestion"),
                json.optString("dinnerGuidance"),
                json.optString("finalMotivationalMessage")
        );
    }
}
