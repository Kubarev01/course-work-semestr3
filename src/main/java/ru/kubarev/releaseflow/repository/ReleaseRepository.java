package ru.kubarev.releaseflow.repository;

import java.util.*;
import org.springframework.data.jpa.repository.*;
import ru.kubarev.releaseflow.domain.*;

public interface ReleaseRepository extends JpaRepository<Release, Long> {
  @EntityGraph(attributePaths = {"owner"})
  List<Release> findAllByOrderByTargetDateAsc();

  @EntityGraph(attributePaths = {"owner"})
  List<Release> findByOwnerUsernameOrderByTargetDateAsc(String username);

  @EntityGraph(attributePaths = {"owner"})
  Optional<Release> findById(Long id);
}
