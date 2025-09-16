package snapmeal.snapmeal.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import snapmeal.snapmeal.domain.enums.MealType;

@Data
public class MealsRequestDto {
    @Schema(description = "분석된 NutritionAnalysis ID", example = "8")
    private Long nutritionId; // // 분석된 NutritionAnalysis의 ID

    @Schema(description = "식사 유형", example = "DINNER", allowableValues = {
            "BREAKFAST","BRUNCH","LUNCH","LINNER","DINNER","MIDNIGHT SNACK","SNACK"
    })
    @JsonProperty("meal_type")
    private MealType mealType;       // 아침, 점심, 저녁, 간식 등

    @Schema(description = "메모 (예: '저녁 약속으로 먹음')", example = "저녁 약속으로 먹음")
    private String memo;       // 자유 메모

    @Schema(description = "식사한 장소", example = "BHC")
    private String location;   // 장소 (ex: "집", "카페")
}