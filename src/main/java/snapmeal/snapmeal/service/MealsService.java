package snapmeal.snapmeal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import snapmeal.snapmeal.converter.MealsConverter;
import snapmeal.snapmeal.domain.Images;
import snapmeal.snapmeal.domain.Meals;
import snapmeal.snapmeal.domain.NutritionAnalysis;
import snapmeal.snapmeal.domain.User;
import snapmeal.snapmeal.global.util.AuthService;
import snapmeal.snapmeal.repository.MealsRepository;
import snapmeal.snapmeal.repository.NutritionAnalysisRepository;
import snapmeal.snapmeal.web.dto.MealsRequestDto;
import snapmeal.snapmeal.web.dto.MealsResponseDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MealsService {

    private final MealsRepository mealsRepository;
    private final NutritionAnalysisRepository nutritionAnalysisRepository;
    private final AuthService authService;
    private final MealsConverter mealsConverter;

    // 식단 저장
    @Transactional
    public MealsResponseDto createMeal(MealsRequestDto request) {
        User user = authService.getCurrentUser();

        NutritionAnalysis nutrition = nutritionAnalysisRepository.findById(request.getNutritionId())
                .orElseThrow(() -> new IllegalArgumentException("영양 분석 정보가 없습니다."));

        Images image = nutrition.getImage();

        Meals meal = Meals.builder()
                .mealType(request.getMealType())
                .memo(request.getMemo())
                .location(request.getLocation())
                .mealDate(LocalDateTime.now())
                .nutrition(nutrition)
                .image(image)
                .user(user)
                .build();

        Meals saved = mealsRepository.save(meal);

        return mealsConverter.toDto(saved);
    }

    // 사용자의 날짜별 식단 조회 (DTO 반환)
    public List<MealsResponseDto> getMealsByDate(LocalDate targetDate) {
        User user = authService.getCurrentUser();

        LocalDate date = (targetDate != null) ? targetDate : LocalDate.now();
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<Meals> meals = mealsRepository.findAllByUserAndCreatedAtBetween(user, startOfDay, endOfDay);

        return mealsConverter.toDtoList(meals);
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
