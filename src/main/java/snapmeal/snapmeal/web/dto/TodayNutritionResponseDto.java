package snapmeal.snapmeal.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "오늘 영양 요약 응답 DTO")
public class TodayNutritionResponseDto {

    @Schema(description = "요약 날짜 (yyyy-MM-dd)", example = "2025-09-30")
    private LocalDate date;

    @Schema(description = "칼로리 요약 정보")
    private NutrientSummary calories;

    @Schema(description = "단백질 요약 정보")
    private NutrientSummary protein;

    @Schema(description = "탄수화물 요약 정보")
    private NutrientSummary carbs;

    @Schema(description = "당 요약 정보")
    private NutrientSummary sugar;

    @Schema(description = "지방 요약 정보")
    private NutrientSummary fat;

    @Schema(description = "나트륨 요약 정보")
    private NutrientSummary sodium;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Schema(description = "영양소 섭취 요약 정보")
    public static class NutrientSummary {

        @Schema(description = "섭취한 양", example = "1450")
        private double consumed;

        @Schema(description = "권장 섭취량", example = "2000")
        private double recommended;

        @Schema(description = "남은 섭취 가능량", example = "550")
        private double remaining;

        @Schema(description = "상태 (부족, 적정, 과다)", example = "적정")
        private String status;
    }
}