// ChallengeRepository.java
package snapmeal.snapmeal.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import snapmeal.snapmeal.domain.User;
import snapmeal.snapmeal.domain.Challenges;
import snapmeal.snapmeal.domain.enums.ChallengeStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ChallengeRepository extends JpaRepository<Challenges, Long> {

    List<Challenges> findAllByUserAndStatusIn(User user, List<ChallengeStatus> statuses);

    List<Challenges> findAllByStatusInAndEndDateLessThanEqual(List<ChallengeStatus> statuses, LocalDate endDate);

    List<Challenges> findAllByUserAndStartDateGreaterThanEqualAndEndDateLessThanEqual(User user, LocalDate start, LocalDate end);

    Optional<Challenges> findByChallengeIdAndUser(Long challengeId, User user);

    boolean existsByUserAndStartDateGreaterThanEqualAndEndDateLessThanEqual(User user, LocalDate start, LocalDate end);
    // ChallengeRepository
    void deleteAllByUserAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
            User user, LocalDate start, LocalDate end
    );

    List<Challenges> findAllByUserOrderByStartDateDesc(User user);
    void deleteAllByUser(User user);

    List<Challenges> findAllByUser(User user);

}
