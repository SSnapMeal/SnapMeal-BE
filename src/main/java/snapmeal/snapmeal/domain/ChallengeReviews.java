package snapmeal.snapmeal.domain;

import jakarta.persistence.*;
        import lombok.*;
import snapmeal.snapmeal.domain.common.BaseEntity;

/**
 * 챌린지 후기/별점 엔티티
 */
@Entity
@Table(name = "challenge_reviews")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChallengeReviews extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenges challenge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 1~5점
    @Setter
    @Column(name = "rating", nullable = false)
    private Integer rating;

    // 후기 내용
    @Setter
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    public void update(Integer newRating, String newContent) {
        if (newRating != null) this.rating = newRating;
        this.content = newContent;
    }
}
