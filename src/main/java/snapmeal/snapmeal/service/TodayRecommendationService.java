package snapmeal.snapmeal.service;

import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import snapmeal.snapmeal.domain.NutritionAnalysis;
import snapmeal.snapmeal.domain.User;
import snapmeal.snapmeal.global.OpenAiClient;
import snapmeal.snapmeal.global.util.AuthService;
import snapmeal.snapmeal.repository.NutritionAnalysisRepository;
import snapmeal.snapmeal.repository.UserRepository;
import snapmeal.snapmeal.web.dto.TodayRecommendationResponseDto;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TodayRecommendationService {

    private final NutritionAnalysisRepository nutritionAnalysisRepository;
    private final OpenAiClient openAiClient;
    private final UserRepository userRepository;
    private final AuthService authService;

    public TodayRecommendationResponseDto generateRecommendation() throws IOException {
        // 로그인된 사용자 가져오기
        User user = authService.getCurrentUser();

        // 오늘 날짜 기준 조회 범위 설정
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        // 오늘의 영양소 분석 데이터 조회
        List<NutritionAnalysis> todayRecords =
                nutritionAnalysisRepository.findAllByUserAndCreatedAtBetween(user, startOfDay, endOfDay);

        // 총 섭취 칼로리 계산
        int totalCalories = todayRecords.stream()
                .mapToInt(NutritionAnalysis::getCalories)
                .sum();

        // 프롬프트 구성 (권장 칼로리는 AI가 판단하게)
        String prompt = String.format("""
당신은 건강한 식생활과 운동 코치입니다.
사용자의 성별은 %s이고 나이는 %d세입니다.
오늘 섭취 칼로리는 총 %d kcal입니다.

다음과 같이 JSON 형식만 응답하세요. 설명 없이 **반드시 아래 형식만 출력하세요**:

{"recommendedCalories": 2200, "exercise": "운동 추천", "food": "음식 추천"}

이 형식을 반드시 지켜주세요. 설명하지 말고 JSON만 응답하세요.
""", user.getGender(), user.getAge(), totalCalories);


        // AI 응답 받기
        String aiResponse = openAiClient.requestCompletion(
                "당신은 건강한 식생활과 운동 코치입니다.",
                prompt
        );

        // JSON 파싱
        JSONObject json = new JSONObject(aiResponse);

        int recommendedCalories = json.optInt("recommendedCalories", 2000); // 기본 2000
        int remainingCalories = Math.max(0, recommendedCalories - totalCalories);

        // DTO 생성 및 반환
        return new TodayRecommendationResponseDto(
                totalCalories,
                remainingCalories,
                json.optString("exercise"),
                json.optString("food")
        );
    }


}
