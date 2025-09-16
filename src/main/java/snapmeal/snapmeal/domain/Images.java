package snapmeal.snapmeal.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import snapmeal.snapmeal.domain.common.BaseEntity;
import snapmeal.snapmeal.domain.enums.MealType;
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Images extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ImgId;

    @Lob
    @Column(name = "image_url")
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(mappedBy = "image", cascade = CascadeType.ALL)
    private NutritionAnalysis nutritionAnalysis;

    private Integer classId;

    @Lob
    @Column(name = "class_name")
    private String className;
}
