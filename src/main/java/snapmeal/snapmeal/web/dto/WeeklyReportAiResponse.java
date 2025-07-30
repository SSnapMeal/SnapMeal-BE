package snapmeal.snapmeal.web.dto;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WeeklyReportAiResponse {
    private String nutritionSummary;
    private String caloriePattern;
    private String healthGuidance;
}

