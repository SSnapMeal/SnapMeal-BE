package snapmeal.snapmeal.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodSearchDto {

    private String name;        // 샐러드
    private Double kcal;        // 320.0
    private Double carbo;       // 탄수화물
    private Double protein;     // 단백질
    private Double fat;         // 지방
    private Double sugar;       // 당류
    private Double sodium;      // 나트륨 (mg)
}
