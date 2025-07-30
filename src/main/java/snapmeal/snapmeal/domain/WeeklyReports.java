package snapmeal.snapmeal.domain;


import jakarta.persistence.*;
import lombok.*;
import snapmeal.snapmeal.domain.common.BaseEntity;

import java.time.LocalDate;

@Entity
@Table(name = "weekly_reports")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeeklyReports extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    @Column(name = "report_date")
    private LocalDate reportDate;

    @Column(name = "total_calories")
    private Float totalCalories;

    @Column(name = "total_protein")
    private Float totalProtein;

    @Column(name = "total_fat")
    private Float totalFat;

    @Column(name = "total_carbs")
    private Float totalCarbs;

    @Column(name = "recommended_exercise")
    private String recommendedExercise;

    @Column(name = "food_suggestion")
    private String foodSuggestion;

    @Column(name = "nutrition_summary", columnDefinition = "TEXT")
    private String nutritionSummary; // ex. 전체적으로 적게 섭취하는 경향이 있어요 ...

    @Column(name = "calorie_pattern", columnDefinition = "TEXT")
    private String caloriePattern; // ex. 주로 저녁에 칼로리가 높은 음식을 먹었어요 ...

    @Column(name = "health_guidance", columnDefinition = "TEXT")
    private String healthGuidance; // ex. 에너지뿐 아니라 필수 영양소도 챙기며 ...
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
