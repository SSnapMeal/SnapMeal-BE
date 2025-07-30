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

    private String recommendedExercise;
    private String foodSuggestion;

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

