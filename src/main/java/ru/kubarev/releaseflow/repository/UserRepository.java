package ru.kubarev.releaseflow.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
import ru.kubarev.releaseflow.domain.*;
public interface UserRepository extends JpaRepository<AppUser,Long> {
 Optional<AppUser> findByUsername(String username);
 List<AppUser> findAllByOrderByUsernameAsc();
}
