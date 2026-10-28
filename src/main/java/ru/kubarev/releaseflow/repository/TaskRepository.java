package ru.kubarev.releaseflow.repository;
import org.springframework.data.jpa.repository.*;
import java.util.*;
import ru.kubarev.releaseflow.domain.*;
public interface TaskRepository extends JpaRepository<Task,Long> {
 @EntityGraph(attributePaths={"owner","release","release.owner"}) List<Task> findAllByOrderByIdDesc();
 @EntityGraph(attributePaths={"owner","release","release.owner"}) List<Task> findByOwnerUsernameOrderByIdDesc(String username);
 @EntityGraph(attributePaths={"owner","release","release.owner"}) Optional<Task> findById(Long id);
 boolean existsByReleaseId(Long id);
}
