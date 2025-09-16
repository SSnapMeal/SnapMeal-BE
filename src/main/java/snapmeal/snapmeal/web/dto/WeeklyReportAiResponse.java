package snapmeal.snapmeal.web.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;


@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "주간 리포트 AI 응답")
public class WeeklyReportAiResponse {

    @Schema(description = "영양소 섭취 경향 요약", example = "탄수화물 섭취 비율이 높고 단백질 섭취가 부족했습니다.")
    private String nutritionSummary;

    @Schema(description = "칼로리 섭취 패턴 (이모지 + 요약 3개)")
    private CaloriePattern caloriePattern;

    @ArraySchema(
            arraySchema = @Schema(description = "식사 및 생활 가이드 (2개 추천)"),
            schema = @Schema(implementation = GuideItem.class)
    )
    private List<GuideItem> healthGuidance;


    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Schema(description = "칼로리 섭취 패턴")
    public static class CaloriePattern {
        @Schema(description = "대표 이모지", example = "🌙")
        private String emoji;

        @ArraySchema(
                arraySchema = @Schema(description = "짧은 요약 3개"),
                schema = @Schema(example = "저녁에 칼로리 집중")
        )
        private List<String> summaries;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Schema(description = "행동 가이드 항목")
    public static class GuideItem {
        @Schema(description = "제목", example = "1. 영양 균형 잡기")
        private String title;

        @Schema(description = "짧은 설명 줄글", example = "단백질과 비타민을 보충해 전체 영양 균형을 맞추세요.")
        private String description;
    }
}
