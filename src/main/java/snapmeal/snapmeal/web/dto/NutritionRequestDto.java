package snapmeal.snapmeal.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class NutritionRequestDto {

    @Data
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FoodNutritionRequestDto {
        private List<String> foodNames;
        private Long imageId;

    }
    @Data
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TotalNutritionRequestDto{
        private int calories;
        private double protein;
        private double carbs;
        private double sugar;
        private double fat;

        private Long nutritionId;
    }
}
