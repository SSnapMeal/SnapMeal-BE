package snapmeal.snapmeal.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import snapmeal.snapmeal.domain.WeeklyReports;
import snapmeal.snapmeal.web.dto.WeeklyReportAiResponse;
import snapmeal.snapmeal.web.dto.WeeklyReportResponseDto;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WeeklyReportConverter {

    private final ObjectMapper objectMapper;

    public WeeklyReportResponseDto toDto(WeeklyReports report) {
        try {
            // String(JSON) → 객체 변환
            WeeklyReportAiResponse.CaloriePattern caloriePattern =
                    objectMapper.readValue(report.getCaloriePattern(), WeeklyReportAiResponse.CaloriePattern.class);

            List<WeeklyReportAiResponse.GuideItem> healthGuidance =
                    objectMapper.readValue(
                            report.getHealthGuidance(),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, WeeklyReportAiResponse.GuideItem.class)
                    );

            // Builder로 DTO 생성
            return WeeklyReportResponseDto.builder()
                    .reportDate(report.getReportDate())
                    .totalCalories(report.getTotalCalories())
                    .totalProtein(report.getTotalProtein())
                    .totalFat(report.getTotalFat())
                    .totalCarbs(report.getTotalCarbs())
                    .nutritionSummary(report.getNutritionSummary())
                    .caloriePattern(caloriePattern)
                    .healthGuidance(healthGuidance)
                    .build();

        } catch (JsonProcessingException e) {
            throw new RuntimeException("리포트 JSON 파싱 실패", e);
        }
    }
}
