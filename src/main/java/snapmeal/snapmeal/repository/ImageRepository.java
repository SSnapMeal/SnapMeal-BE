package snapmeal.snapmeal.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import snapmeal.snapmeal.domain.Images;
import snapmeal.snapmeal.domain.User;

public interface ImageRepository extends JpaRepository<Images, Long> {
    void deleteAllByUser(User user);
    List<Images> findAllByUser(User user);
}
