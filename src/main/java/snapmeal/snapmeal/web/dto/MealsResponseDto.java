package snapmeal.snapmeal.web.dto;

import lombok.Builder;
import lombok.Data;
import snapmeal.snapmeal.domain.Meals;
import snapmeal.snapmeal.domain.enums.MealType;

import java.time.LocalDateTime;

@Data
@Builder
public class MealsResponseDto {
    private Long mealId;
    private MealType mealType;
    private String memo;
    private String location;
    private LocalDateTime mealDate;
    private double calories;
    private double protein;
    private double carbs;
    private double sugar;
    private double fat;

    private String className;
    private String imageUrl;

    public static MealsResponseDto from(Meals meal) {
        return MealsResponseDto.builder()
                .mealId(meal.getMealId())
                .mealType(meal.getMealType())
                .memo(meal.getMemo())
                .location(meal.getLocation())
                .mealDate(meal.getMealDate())
                .calories(meal.getNutrition().getCalories())
                .protein(meal.getNutrition().getProtein())
                .carbs(meal.getNutrition().getCarbs())
                .sugar(meal.getNutrition().getSugar())
                .fat(meal.getNutrition().getFat())
                .className(meal.getImage().getClassName())
                .imageUrl(meal.getImage().getImageUrl())
                .build();
    }
}
