package ru.kubarev.releaseflow.domain;

import jakarta.persistence.*;
import java.util.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "USERS")
public class AppUser {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 80)
  private String username;

  @Column(nullable = false, length = 80)
  private String displayName;

  @Column(nullable = false, length = 60)
  private String password;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(
      name = "USER_ROLES",
      joinColumns = @JoinColumn(name = "user_id"),
      uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "role"}))
  @Column(name = "role", nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  private Set<Role> roles = new HashSet<>();

  public boolean isAdmin() {
    return roles.contains(Role.ADMIN);
  }

  public boolean canWrite() {
    return isAdmin() || roles.contains(Role.USER);
  }

  public String getRoleLabel() {
    return isAdmin() ? "Администратор" : canWrite() ? "Участник" : "Только чтение";
  }
}
