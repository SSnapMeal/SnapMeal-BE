package snapmeal.snapmeal.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Builder
@Getter
public class TodayNutritionResponseDto {

    private LocalDate date;
    private int totalCalories;
    private double totalProtein;
    private double totalCarbs;
    private double totalSugar;
    private double totalFat;

}
