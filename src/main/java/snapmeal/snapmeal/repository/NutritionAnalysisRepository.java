package snapmeal.snapmeal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import snapmeal.snapmeal.domain.NutritionAnalysis;
import snapmeal.snapmeal.domain.User;

import java.time.LocalDateTime;
import java.util.List;

public interface NutritionAnalysisRepository extends JpaRepository<NutritionAnalysis, Long> {
    List<NutritionAnalysis> findAllByUserAndCreatedAtBetween(User user, LocalDateTime start, LocalDateTime end);
    void deleteAllByUser(User user);
}
