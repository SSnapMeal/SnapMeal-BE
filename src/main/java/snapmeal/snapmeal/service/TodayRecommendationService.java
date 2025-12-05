package snapmeal.snapmeal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import snapmeal.snapmeal.domain.NutritionAnalysis;
import snapmeal.snapmeal.domain.User;
import snapmeal.snapmeal.global.OpenAiClient;
import snapmeal.snapmeal.global.code.ErrorCode;
import snapmeal.snapmeal.global.handler.RecommendationHandler;
import snapmeal.snapmeal.global.util.AuthService;
import snapmeal.snapmeal.global.util.OpenAiConverter;
import snapmeal.snapmeal.repository.NutritionAnalysisRepository;
import snapmeal.snapmeal.repository.UserRepository;
import snapmeal.snapmeal.web.dto.TodayRecommendationResponseDto;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TodayRecommendationService {

    private final NutritionAnalysisRepository nutritionAnalysisRepository;
    private final OpenAiClient openAiClient;
    private final AuthService authService;
    private final RedisTemplate<String, TodayRecommendationResponseDto> recommendationRedisTemplate;


    private static final String CACHE_PREFIX = "todayRecommendation:";

    public TodayRecommendationResponseDto generateRecommendation() {
        try {
            User user = authService.getCurrentUser();
            log.info("[TodayRecommendation] 현재 사용자 ID: {}", user.getId());

            String todayKey = CACHE_PREFIX + user.getId() + ":" + LocalDate.now();
            log.debug("[TodayRecommendation] 캐시 키: {}", todayKey);

            TodayRecommendationResponseDto cached =
                    recommendationRedisTemplate.opsForValue().get(todayKey);
            if (cached != null) {
                log.info("[TodayRecommendation] 캐시에서 결과 반환");
                return cached;
            }

            int totalCalories = calculateTodayCalories(user);
            log.info("[TodayRecommendation] 오늘 섭취 칼로리 합계: {}", totalCalories);

            String prompt = OpenAiConverter.buildTodayRecommendationPrompt(
                    user.getGender().getDisplayName(),
                    user.getAge(),
                    totalCalories
            );
            log.debug("[TodayRecommendation] 프롬프트 생성 완료: {}", prompt);

            String aiResponse = openAiClient.requestCompletion(
                    "당신은 건강한 식생활과 운동 코치입니다.",
                    prompt
            );
            log.debug("[TodayRecommendation] OpenAI 응답: {}", aiResponse);

            String cleanResponse = extractJson(aiResponse);
            log.debug("[TodayRecommendation] JSON 정제 응답: {}", cleanResponse);

            JSONObject json = new JSONObject(cleanResponse);

            int recommendedCalories = json.optInt("recommendedCalories", 2000);
            int remainingCalories = Math.max(0, recommendedCalories - totalCalories);
            log.info("[TodayRecommendation] 권장 칼로리: {}, 남은 칼로리: {}",
                    recommendedCalories, remainingCalories);

            TodayRecommendationResponseDto dto = TodayRecommendationResponseDto.builder()
                    .consumedCalories(totalCalories)
                    .remainingCalories(remainingCalories)
                    .exercises(parseExercises(json.getJSONArray("exercises")))
                    .foods(parseFoods(json.getJSONArray("foods")))
                    .build();

            recommendationRedisTemplate.opsForValue().set(todayKey, dto, Duration.ofDays(1));
            log.info("[TodayRecommendation] 추천 결과 캐시에 저장 완료");

            return dto;

        } catch (Exception e) {
            log.error("[TodayRecommendation] 추천 생성 실패: {}", e.getMessage(), e);
            throw new RecommendationHandler(ErrorCode.RECOMMENDATION_GENERATION_FAILED);
        }
    }

    private int calculateTodayCalories(User user) {

        List<NutritionAnalysis> todayRecords =
                nutritionAnalysisRepository.findTodayRecords(user, LocalDate.now());

        log.info("🔥 오늘 영양 데이터 개수: {}", todayRecords.size());
        log.info("🔥 오늘 영양 calories 목록: {}",
                todayRecords.stream().map(NutritionAnalysis::getCalories).toList());

        return todayRecords.stream()
                .mapToInt(NutritionAnalysis::getCalories)
                .sum();
    }


    private String extractJson(String response) {
        String trimmed = response.trim();
        if (trimmed.startsWith("```")) {
            return trimmed.replaceAll("```(json)?", "").trim();
        }
        return trimmed;
    }

    private List<TodayRecommendationResponseDto.ExerciseRecommendationDto> parseExercises(JSONArray arr) {
        List<TodayRecommendationResponseDto.ExerciseRecommendationDto> list = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            list.add(TodayRecommendationResponseDto.ExerciseRecommendationDto.builder()
                    .name(obj.optString("name"))
                    .calories(obj.optInt("calories"))
                    .duration(obj.optString("duration"))
                    .repeat(obj.optInt("repeat"))
                    .category(obj.optString("category"))
                    .emoji(obj.optString("emoji"))
                    .build());
        }
        return list;
    }

    private List<TodayRecommendationResponseDto.FoodRecommendationDto> parseFoods(JSONArray arr) {
        List<TodayRecommendationResponseDto.FoodRecommendationDto> list = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            list.add(TodayRecommendationResponseDto.FoodRecommendationDto.builder()
                    .name(obj.optString("name"))
                    .calories(obj.optInt("calories"))
                    .benefit(obj.optString("benefit"))
                    .emoji(obj.optString("emoji"))
                    .build());
        }
        return list;
    }

}