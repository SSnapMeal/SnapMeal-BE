package snapmeal.snapmeal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import snapmeal.snapmeal.domain.User;
import snapmeal.snapmeal.domain.Challenges;
import snapmeal.snapmeal.domain.ChallengeReviews;

import java.util.List;

public interface ChallengeReviewRepository extends JpaRepository<ChallengeReviews, Long> {
    List<ChallengeReviews> findAllByChallenge(Challenges challenge);
    List<ChallengeReviews> findAllByUser(User user);
}
