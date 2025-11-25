package snapmeal.snapmeal.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import snapmeal.snapmeal.domain.Meals;
import snapmeal.snapmeal.domain.User;

public interface MealsRepository extends JpaRepository<Meals, Long> {
    List<Meals> findAllByUser(User user);

    boolean existsByUserAndMealDateBetweenAndMenuContainingIgnoreCase(
            snapmeal.snapmeal.domain.User user,
            java.time.LocalDateTime startInclusive,
            java.time.LocalDateTime endInclusive,
            String targetKeyword
    );
    List<Meals> findAllByUserAndCreatedAtBetween(User user, LocalDateTime start, LocalDateTime end);

    List<Meals> findAllByUserOrderByMealDateDesc(User user);
    void deleteAllByUser(User user);
}