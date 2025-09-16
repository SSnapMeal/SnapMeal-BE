package snapmeal.snapmeal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import snapmeal.snapmeal.domain.Images;
import snapmeal.snapmeal.domain.Meals;
import snapmeal.snapmeal.domain.NutritionAnalysis;
import snapmeal.snapmeal.domain.User;
import snapmeal.snapmeal.global.util.AuthService;
import snapmeal.snapmeal.repository.MealsRepository;
import snapmeal.snapmeal.repository.NutritionAnalysisRepository;
import snapmeal.snapmeal.web.dto.MealsRequestDto;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MealsService {

    private final MealsRepository mealsRepository;
    private final NutritionAnalysisRepository nutritionAnalysisRepository;
    private final AuthService authService;

    // 식단 저장
    public Meals createMeal(MealsRequestDto request) {
        User user = authService.getCurrentUser();

        NutritionAnalysis nutrition = nutritionAnalysisRepository.findById(request.getNutritionId())
                .orElseThrow(() -> new IllegalArgumentException("영양 분석 정보가 없습니다."));

        Images image = nutrition.getImage(); // nutrition에 이미 연결되어 있음

        Meals meal = Meals.builder()
                .mealType(request.getMealType())  // enum 사용
                .memo(request.getMemo())
                .location(request.getLocation())
                .mealDate(LocalDateTime.now())  // 또는 request에서 받기
                .nutrition(nutrition)
                .image(image)
                .user(user)
                .build();

        return mealsRepository.save(meal);
    }

    // 사용자의 모든 식단 조회
    public List<Meals> getAllMeals() {
        User user = authService.getCurrentUser();
        return mealsRepository.findAllByUser(user);
    }

    // 사용자의 식단 개별 조회
    public Meals getMeal(Long mealId) {
        User user = authService.getCurrentUser();
        Meals meal = mealsRepository.findById(mealId)
                .orElseThrow(() -> new IllegalArgumentException("해당 식단이 없습니다. id=" + mealId));

        if (!meal.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("해당 식단에 접근할 수 없습니다.");
        }

        return meal;
    }

    // 식단 수정
    public Meals updateMeal(Long mealId, MealsRequestDto requestDto) {
        Meals meal = getMeal(mealId);

        meal.update(requestDto.getMealType(), requestDto.getMemo(), requestDto.getLocation());
        return mealsRepository.save(meal);
    }

    // 식단 삭제
    public void deleteMeal(Long mealId) {
        Meals meal = getMeal(mealId);
        mealsRepository.delete(meal);
    }
}
