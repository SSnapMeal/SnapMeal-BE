package snapmeal.snapmeal.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import snapmeal.snapmeal.domain.Images;
import snapmeal.snapmeal.domain.Meals;
import snapmeal.snapmeal.domain.NutritionAnalysis;
import snapmeal.snapmeal.domain.enums.MealType;

import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MealsResponseDto {
    private Long mealId;
    private MealType mealType;
    private String memo;
    private String location;
    private LocalDateTime mealDate;

    private Integer calories;
    private Double protein;
    private Double carbs;
    private Double sugar;
    private Double fat;
    private Double sodium;
    private List<String> classNames;
    private String menu;
    private String imageUrl;

    public static MealsResponseDto from(Meals meal) {

        NutritionAnalysis nutrition = meal.getNutrition();
        Images image = meal.getImage();

        Integer calories = (nutrition != null) ? nutrition.getCalories() : 0;
        Double protein   = (nutrition != null) ? nutrition.getProtein()  : 0.0;
        Double carbs     = (nutrition != null) ? nutrition.getCarbs()    : 0.0;
        Double sugar     = (nutrition != null) ? nutrition.getSugar()    : 0.0;
        Double fat       = (nutrition != null) ? nutrition.getFat()      : 0.0;
        Double sodium    = (nutrition != null) ? nutrition.getSodium()   : 0.0;

//        List<String> classNames = (image != null && image.getClassName() != null)
//                ? List.of(image.getClassName())
//                : List.of();

        String imageUrl = (image != null) ? image.getImageUrl() : null;

        return MealsResponseDto.builder()
                .mealId(meal.getMealId())
                .mealType(meal.getMealType())
                .memo(meal.getMemo())
                .location(meal.getLocation())
                .mealDate(meal.getMealDate())
                .calories(calories)
                .protein(protein)
                .carbs(carbs)
                .sugar(sugar)
                .fat(fat)
                .sodium(sodium)
//                .classNames(classNames)
                .imageUrl(imageUrl)
                .menu(meal.getMenu())
                .build();
    }

}
