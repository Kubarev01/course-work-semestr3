package ru.kubarev.releaseflow.service;

import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.kubarev.releaseflow.domain.*;
import ru.kubarev.releaseflow.dto.RegistrationForm;
import ru.kubarev.releaseflow.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
  private final UserRepository repository;
  private final PasswordEncoder encoder;
  private final AuditService audit;

  public static String normalize(String s) {
    return s.trim().toLowerCase(Locale.ROOT);
  }

  public AppUser current() {
    return repository
        .findByUsername(SecurityContextHolder.getContext().getAuthentication().getName())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
  }

  @Transactional
  public void register(RegistrationForm form) {
    String name = normalize(form.getUsername());
    if (repository.findByUsername(name).isPresent())
      throw new IllegalArgumentException("Этот логин уже занят");
    AppUser user = new AppUser();
    user.setUsername(name);
    user.setDisplayName(form.getDisplayName().trim());
    user.setPassword(encoder.encode(form.getPassword()));
    user.setRoles(EnumSet.of(Role.READ_ONLY));
    repository.saveAndFlush(user);
    audit.record(name, "REGISTER", "Регистрация с ролью READ_ONLY");
  }

  public void requireAdmin() {
    if (!current().isAdmin()) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
  }

  @Transactional
  public void changeRole(Long id, Role role) {
    requireAdmin();
    AppUser actor = current();
    AppUser u =
        repository
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    if (u.getId().equals(actor.getId()) && role != Role.ADMIN)
      throw new IllegalArgumentException("Нельзя снять у себя роль администратора");
    u.setRoles(
        role == Role.READ_ONLY ? EnumSet.of(Role.READ_ONLY) : EnumSet.of(Role.READ_ONLY, role));
    repository.save(u);
    audit.record(actor.getUsername(), "ROLE_CHANGE", "Пользователь #" + id + " → " + role.name());
  }

  public List<AppUser> all() {
    requireAdmin();
    return repository.findAllByOrderByUsernameAsc();
  }
}
