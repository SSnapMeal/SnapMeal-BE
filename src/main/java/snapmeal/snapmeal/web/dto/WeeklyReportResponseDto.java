package snapmeal.snapmeal.web.dto;

import lombok.*;
import snapmeal.snapmeal.domain.WeeklyReportDetails;
import snapmeal.snapmeal.domain.WeeklyReports;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeeklyReportResponseDto {

    private LocalDate reportDate;
    private Float totalCalories;
    private Float totalProtein;
    private Float totalFat;
    private Float totalCarbs;

<<<<<<< Updated upstream
    private String recommendedExercise;
    private String foodSuggestion;

=======
    @Schema(description = "총 섭취 나트륨(g)", example = "75.4")
    private Float totalSodium;

    @Schema(description = "영양소 섭취 요약", example = "이번 주는 단백질이 부족하고 탄수화물 섭취가 많았습니다.")
>>>>>>> Stashed changes
    private String nutritionSummary;
    private String caloriePattern;
    private String healthGuidance;

    public WeeklyReportResponseDto(WeeklyReports report) {
        this.reportDate = report.getReportDate();
        this.totalCalories = report.getTotalCalories();
        this.totalProtein = report.getTotalProtein();
        this.totalFat = report.getTotalFat();
        this.totalCarbs = report.getTotalCarbs();

        this.recommendedExercise = report.getRecommendedExercise();
        this.foodSuggestion = report.getFoodSuggestion();

        this.nutritionSummary = report.getNutritionSummary();
        this.caloriePattern = report.getCaloriePattern();
        this.healthGuidance = report.getHealthGuidance();
    }
}

