package snapmeal.snapmeal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import snapmeal.snapmeal.domain.Meals;
import snapmeal.snapmeal.domain.User;
import snapmeal.snapmeal.domain.enums.MealType;

import java.util.List;

public interface MealsRepository extends JpaRepository<Meals, Long> {
    List<Meals> findAllByUser(User user);

    boolean existsByUserAndMealDateBetweenAndMenuContainingIgnoreCase(
            snapmeal.snapmeal.domain.User user,
            java.time.LocalDateTime startInclusive,
            java.time.LocalDateTime endInclusive,
            String targetKeyword
    );
}