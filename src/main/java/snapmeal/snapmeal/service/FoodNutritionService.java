package snapmeal.snapmeal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import snapmeal.snapmeal.converter.NutritionConverter;
import snapmeal.snapmeal.domain.Images;
import snapmeal.snapmeal.domain.NutritionAnalysis;
import snapmeal.snapmeal.domain.User;
import snapmeal.snapmeal.global.OpenAiClient;
import snapmeal.snapmeal.repository.ImageRepository;
import snapmeal.snapmeal.repository.NutritionAnalysisRepository;
import snapmeal.snapmeal.web.dto.NutritionRequestDto;
import snapmeal.snapmeal.global.util.OpenAiConverter;
import snapmeal.snapmeal.global.util.AuthService;
import snapmeal.snapmeal.web.dto.TodayNutritionResponseDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FoodNutritionService {
    private final OpenAiClient openAiClient;
    private final ImageRepository imagesRepository;
    private final AuthService authService;
    private final NutritionAnalysisRepository nutritionAnalysisRepository;
    private final NutritionConverter nutritionConverter;

    @Transactional
    public NutritionRequestDto.TotalNutritionRequestDto analyze(NutritionRequestDto.FoodNutritionRequestDto request) {
        try {
            String systemPrompt = "당신은 영양 분석 전문가입니다.";
            String userPrompt = OpenAiConverter.getFoodNutritionInfo(request.getFoodNames());
            String response = openAiClient.requestCompletion(systemPrompt, userPrompt);
            System.out.println("🥣 OpenAI 응답: " + response);

            NutritionRequestDto.TotalNutritionRequestDto result = NutritionConverter.fromOpenAiJson(response);

            User currentUser = authService.getCurrentUser();
            Images image = imagesRepository.findById(request.getImageId())
                    .orElseThrow(() -> new IllegalArgumentException("이미지를 찾을 수 없습니다."));

            NutritionAnalysis analysis = NutritionAnalysis.builder()
                    .image(image)
                    .calories(result.getCalories())
                    .protein(result.getProtein())
                    .carbs(result.getCarbs())
                    .sugar(result.getSugar())
                    .fat(result.getFat())
                    .foodNames(String.join(", ", request.getFoodNames()))
                    .user(currentUser)
                    .build();

            NutritionAnalysis saved = nutritionAnalysisRepository.save(analysis);
            result.setNutritionId(saved.getId());
            return result;

        } catch (Exception e) {
            return new NutritionRequestDto.TotalNutritionRequestDto(0, 0, 0, 0, 0, 0L);
        }
    }
    @Transactional(readOnly = true)
    public TodayNutritionResponseDto getTodaySummary() {
        User user = authService.getCurrentUser();

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        List<NutritionAnalysis> analyses =
                nutritionAnalysisRepository.findAllByUserAndCreatedAtBetween(user, startOfDay, endOfDay);

        int totalCalories = 0;
        double totalProtein = 0;
        double totalCarbs = 0;
        double totalSugar = 0;
        double totalFat = 0;

        for (NutritionAnalysis na : analyses) {
            totalCalories += na.getCalories();
            totalProtein += na.getProtein();
            totalCarbs += na.getCarbs();
            totalSugar += na.getSugar();
            totalFat += na.getFat();
        }

        return nutritionConverter.toSummaryDto(today, totalCalories, totalProtein, totalCarbs, totalSugar, totalFat);
    }

}
