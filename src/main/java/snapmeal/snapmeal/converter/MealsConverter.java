package snapmeal.snapmeal.converter;

import org.springframework.stereotype.Component;
import snapmeal.snapmeal.domain.Meals;
import snapmeal.snapmeal.web.dto.MealsResponseDto;

import java.util.List;

@Component
public class MealsConverter {

    public MealsResponseDto toDto(Meals meal) {
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

    public List<MealsResponseDto> toDtoList(List<Meals> meals) {
        return meals.stream()
                .map(this::toDto)
                .toList();
    }
}
