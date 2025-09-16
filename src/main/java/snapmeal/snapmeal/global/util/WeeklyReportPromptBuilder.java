package snapmeal.snapmeal.global.util;

import org.springframework.stereotype.Component;
import snapmeal.snapmeal.domain.NutritionAnalysis;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class WeeklyReportPromptBuilder {

    public String buildWeeklyReportPrompt(List<NutritionAnalysis> list, float totalCal, float totalCarb) {
        return String.format("""
            아래는 사용자의 일주일간 식단 정보입니다.
            총 칼로리: %.1f kcal, 총 탄수화물: %.1f g
            하루별 섭취 기록:
            %s

            아래 세 항목을 각각 문단으로 설명하고 JSON으로 응답해주세요:
            1. 영양소 섭취 경향 요약 (짧은 설명 2줄)
            2. 저녁 시간대 위주의 섭취 경향 및 수치 요약 (대표 이모지 1개 + 요약 3개)
            3. 식사 및 생활 가이드 (번호 + 소제목 + 짧은 설명, 총 2개)

            응답 예시:
            {
              "nutritionSummary": "...",
              "caloriePattern": {
                "emoji": "🌙",
                "summaries": ["...", "...", "..."]
              },
              "healthGuidance": [
                { "title": "영양 균형 잡기", "description": "..." },
                { "title": "저녁·야식은 가볍게", "description": "..." }
              ]
            }
            """,
                totalCal, totalCarb,
                list.stream()
                        .map(n -> "- " + n.getFoodNames() + ": " + n.getCalories() + "kcal")
                        .collect(Collectors.joining("\n"))
        );
    }
}

