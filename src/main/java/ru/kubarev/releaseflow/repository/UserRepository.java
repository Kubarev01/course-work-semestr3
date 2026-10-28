package ru.kubarev.releaseflow.repository;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.kubarev.releaseflow.domain.*;

public interface UserRepository extends JpaRepository<AppUser, Long> {
  Optional<AppUser> findByUsername(String username);

  List<AppUser> findAllByOrderByUsernameAsc();
}
