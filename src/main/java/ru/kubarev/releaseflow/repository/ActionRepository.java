package ru.kubarev.releaseflow.repository;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.kubarev.releaseflow.domain.UserAction;

public interface ActionRepository extends JpaRepository<UserAction, Long> {
  List<UserAction> findTop200ByOrderByIdDesc();

  List<UserAction> findTop200ByActorOrderByIdDesc(String actor);
}
