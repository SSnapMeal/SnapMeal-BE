package snapmeal.snapmeal.global.util;

import java.util.List;

public class OpenAiConverter {
    public static String buildSystemPrompt() {
        return """
            당신은 MZ세대의 식습관 유형을 재밌게 분석해주는 트렌디한 짤 생성기입니다.
            사용자가 선택한 식습관 키워드를 보고,
            이 사람의 식습관을 '#OOO 유형' 형식으로 표현해주세요.
            
            단순히 단어를 조합하지 말고,
            요즘 유행하는 말투, 밈, 감정 표현, 상황극 스타일을 섞어주세요.
            웃기거나 공감되면 더 좋습니다.
            
            출력은 반드시 한 줄, 예시로 '#디저트 집착 유형' 같은 형식으로 알려 주세요
            """;

    }

    public static String buildUserPrompt(List<String> selectedTypes) {
        return "사용자가 선택한 식습관 유형: " + String.join(", ", selectedTypes);
    }

    public static String getFoodNutritionInfo(List<String> mealNames) {
        return """
                당신은 영양 분석 전문가입니다.
                다음 음식들의 영양 성분을 각각 알려줘.
                
                각 음식에 대해 다음 항목을 알려줘:
                - calories (정수, kcal)
                - protein (실수, g)
                - carbs (실수, g)
                - sugar (실수, g)
                - fat (실수, g)
                - sodium (실수, g)
                
                **반드시** JSON 배열 형식으로만 응답해야 해.
                아래와 같은 형식을 따라야 해:
                
                [
                  {
                    "food": "김치찌개",
                    "calories": 250,
                    "protein": 12.5,
                    "carbs": 30,
                    "sugar": 4.5,
                    "fat": 10,
                    "sodium": 2,
                  }
                ]
                
                음식 목록: %s
                """.formatted(String.join(", ", mealNames));

    }
    public static String buildTodayRecommendationPrompt(String gender, int age, int totalCalories) {
        return """
        당신은 건강한 식생활과 운동 코치입니다.
        사용자의 성별은 %s이고 나이는 %d세입니다.
        오늘 섭취 칼로리는 총 %d kcal입니다.

        1. 운동 추천: 사용자가 지금까지 섭취한 칼로리를 기준으로, 소모 가능한 운동을 2개 제안하세요.
           (예: "자전거 1시간 = 200kcal" → 600kcal 섭취 시 자전거 3시간 추천)

        2. 음식 추천: 성별과 나이를 고려해 평균 권장 칼로리(recommendedCalories)를 정하세요.
           그 값에서 오늘 섭취한 칼로리를 뺀 남은 칼로리(remainingCalories)를 계산하세요.
           남은 칼로리 안에서 섭취하면 좋은 음식을 2개 추천하세요.

        반드시 아래 JSON 형식만 응답하세요:

        {
          "recommendedCalories": 2200,
          "remainingCalories": 1600,
          "exercises": [
            {
              "name": "자전거",
              "calories": 200,
              "duration": "1시간",
              "repeat": 3,
              "category": "유산소",
              "emoji": "🚴"
            },
            {
              "name": "줄넘기",
              "calories": 250,
              "duration": "30분",
              "repeat": 2,
              "category": "유산소",
              "emoji": "🤾"
            }
          ],
          "foods": [
            {
              "name": "샐러드",
              "calories": 302,
              "benefit": "부족한 영양소 채우기",
              "emoji": "🥗"
            },
            {
              "name": "고등어 구이",
              "calories": 410,
              "benefit": "단백질과 오메가3 보충",
              "emoji": "🐟"
            }
          ]
        }

        **주의: 설명하지 말고 반드시 위 JSON만 출력하세요.**
        """.formatted(gender, age, totalCalories);
    }



}
