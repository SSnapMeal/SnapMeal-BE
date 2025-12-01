package snapmeal.snapmeal.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import snapmeal.snapmeal.domain.WeeklyReportDetails;
import snapmeal.snapmeal.domain.WeeklyReports;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeeklyReportResponseDto {


    @Schema(description = "리포트 주차 시작일(월요일)", example = "2025-07-14")
    private LocalDate reportDate;

    @Schema(description = "총 섭취 칼로리(kcal)", example = "14320.5")
    private Float totalCalories;

    @Schema(description = "총 섭취 단백질(g)", example = "480.2")
    private Float totalProtein;

    @Schema(description = "총 섭취 당(g)", example = "490.2")
    private Float totalSugar;

    @Schema(description = "총 섭취 지방(g)", example = "350.7")
    private Float totalFat;

    @Schema(description = "총 섭취 탄수화물(g)", example = "1850.9")
    private Float totalCarbs;

    @Schema(description = "총 섭취 나트륨(g)", example = "35.9")
    private Float totalSodium;

    @Schema(description = "영양소 섭취 요약", example = "이번 주는 단백질이 부족하고 탄수화물 섭취가 많았습니다.")
    private String nutritionSummary;

    @Schema(description = "칼로리 패턴 요약")
    private WeeklyReportAiResponse.CaloriePattern caloriePattern;

    @Schema(description = "식사 및 생활 가이드")
    private List<WeeklyReportAiResponse.GuideItem> healthGuidance;

}
