package snapmeal.snapmeal.repository;

import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import snapmeal.snapmeal.domain.NutritionAnalysis;
import snapmeal.snapmeal.domain.User;

import java.time.LocalDateTime;
import java.util.List;

public interface NutritionAnalysisRepository extends JpaRepository<NutritionAnalysis, Long> {
    List<NutritionAnalysis> findAllByUserAndCreatedAtBetween(User user, LocalDateTime start, LocalDateTime end);
    void deleteAllByUser(User user);
    @Query("""
    SELECT n FROM NutritionAnalysis n
    WHERE n.user = :user
      AND DATE(n.createdAt) = :today
""")
    List<NutritionAnalysis> findTodayRecords(User user, LocalDate today);

}
