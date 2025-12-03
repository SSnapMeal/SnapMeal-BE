package snapmeal.snapmeal.web.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class NutritionOcrResponseDto {
    private Integer nutritionId;
    private Integer calories;
    private Double protein;
    private Double carbs;
    private Double sugar;
    private Double fat;
    private Double sodium;
}

