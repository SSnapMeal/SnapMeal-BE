package snapmeal.snapmeal.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import snapmeal.snapmeal.domain.common.BaseEntity;
import snapmeal.snapmeal.domain.enums.MealType;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Meals extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mealId;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type")
    private MealType mealType;

    private String memo;
    private String location;
    private LocalDateTime mealDate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    private Images image;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nutrition_id")
    private NutritionAnalysis nutrition;

    public void update(MealType mealType, String memo, String location) {
        this.mealType = mealType;
        this.memo = memo;
        this.location = location;
    }

    @Column(name = "menu")
    private String menu;
}