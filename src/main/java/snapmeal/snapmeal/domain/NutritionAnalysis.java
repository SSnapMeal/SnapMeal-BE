package snapmeal.snapmeal.domain;

import jakarta.persistence.*;
import lombok.*;
import snapmeal.snapmeal.domain.common.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "nutrition_analysis")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NutritionAnalysis extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Lob
    @Column(nullable = false)
    private String foodNames;

    private Integer calories;
    private Double protein;
    private Double carbs;
    private Double sugar;
    private Double fat;
    private Double sodium;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    private Images image;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
