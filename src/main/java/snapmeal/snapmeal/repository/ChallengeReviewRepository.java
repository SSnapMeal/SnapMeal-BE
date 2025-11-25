package snapmeal.snapmeal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import snapmeal.snapmeal.domain.User;
import snapmeal.snapmeal.domain.Challenges;
import snapmeal.snapmeal.domain.ChallengeReviews;

import java.util.List;
import java.util.Optional;

public interface ChallengeReviewRepository extends JpaRepository<ChallengeReviews, Long> {
    Optional<ChallengeReviews> findByChallenge(Challenges challenge);

    // 내가 쓴 리뷰 전체, 최신순
    List<ChallengeReviews> findAllByUserOrderByCreatedAtDesc(User user);
}
