package ru.kubarev.releaseflow.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.kubarev.releaseflow.domain.*;
import ru.kubarev.releaseflow.service.UserService;

@ControllerAdvice
@RequiredArgsConstructor
public class PageAdvice {
  private final UserService users;

  @ModelAttribute
  public void shared(Model model, Authentication auth) {
    if (auth != null && auth.isAuthenticated()) model.addAttribute("viewer", users.current());
    model.addAttribute("taskStatuses", TaskStatus.values());
    model.addAttribute("priorities", Priority.values());
    model.addAttribute("releaseStatuses", ReleaseStatus.values());
    model.addAttribute("roles", Role.values());
  }
}
