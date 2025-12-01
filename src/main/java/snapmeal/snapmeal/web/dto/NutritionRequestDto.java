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
        private Integer calories;
        private Double protein;
        private Double carbs;
        private Double sugar;
        private Double fat;
        private Double sodium;

        private Long nutritionId;
    }
}