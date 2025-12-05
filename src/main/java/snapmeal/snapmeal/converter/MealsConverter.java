package snapmeal.snapmeal.converter;

import java.util.Arrays;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import snapmeal.snapmeal.domain.Meals;
import snapmeal.snapmeal.domain.NutritionAnalysis;
import snapmeal.snapmeal.domain.Images;
import snapmeal.snapmeal.web.dto.MealsResponseDto;

import java.util.List;

@Component
public class MealsConverter {

    // 단일 Meals 엔티티 -> 응답 DTO 로 변환
    public MealsResponseDto toDto(Meals meal) {

        // 영양분석, 이미지 엔티티를 먼저 꺼내놓고, null 일 수도 있다는 걸 전제로 둔다.
        NutritionAnalysis nutrition = meal.getNutrition();
        Images image = meal.getImage();

        // 영양분석이 있을 때만 값 꺼내고, 없으면 null 로 둔다.
        Integer calories = (nutrition != null) ? nutrition.getCalories() : 0;
        Double protein   = (nutrition != null) ? nutrition.getProtein()  : 0.0;
        Double carbs     = (nutrition != null) ? nutrition.getCarbs()    : 0.0;
        Double sugar     = (nutrition != null) ? nutrition.getSugar()    : 0.0;
        Double fat       = (nutrition != null) ? nutrition.getFat()      : 0.0;
        Double sodium    = (nutrition != null) ? nutrition.getSodium()   : 0.0;

        List<String> foodNames = null;

        if (nutrition != null && nutrition.getFoodNames() != null) {

            String raw = nutrition.getFoodNames();

            if (!raw.equals("영양성분표 OCR 분석")) {
                foodNames = Arrays.stream(raw.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isBlank())
                        .collect(Collectors.toList());
            }
        }
        String imageUrl  = (image != null) ? image.getImageUrl()  : null;

        return MealsResponseDto.builder()
                .mealId(meal.getMealId())
                .mealType(meal.getMealType())
                .memo(meal.getMemo())
                .location(meal.getLocation())
                .mealDate(meal.getMealDate())
                .menu(meal.getMenu())
                // 영양 정보 (없으면 null 로 내려감)
                .calories(calories)
                .protein(protein)
                .carbs(carbs)
                .sugar(sugar)
                .fat(fat)
                .sodium(sodium)

                // 이미지 정보 (없으면 null)
                .classNames(foodNames)
                .imageUrl(imageUrl)
                .build();
    }

    // 엔티티 리스트 -> DTO 리스트 변환
    public List<MealsResponseDto> toDtoList(List<Meals> meals) {
        return meals.stream()
                .map(this::toDto)
                .toList();
    }
}
