package snapmeal.snapmeal.repository;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import snapmeal.snapmeal.domain.User;
import snapmeal.snapmeal.domain.WeeklyReports;
@Repository
public interface WeeklyReportRepository extends JpaRepository<WeeklyReports, Long> {

    Optional<WeeklyReports> findTopByUserOrderByReportDateDesc(User user);
    Optional<WeeklyReports> findByUserAndReportDate(User user, LocalDate reportDate);
    void deleteAllByUser(User user);
}
