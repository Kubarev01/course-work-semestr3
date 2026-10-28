package ru.kubarev.releaseflow.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.kubarev.releaseflow.domain.Role;
import ru.kubarev.releaseflow.repository.ActionRepository;
import ru.kubarev.releaseflow.service.*;

@Controller
@RequiredArgsConstructor
public class AdminController {
  private final UserService users;
  private final AuditService audit;
  private final ActionRepository actions;

  @GetMapping("/admin/users")
  public String users(Model m) {
    m.addAttribute("users", users.all());
    return "users";
  }

  @PostMapping("/admin/users/{id}/role")
  public String role(@PathVariable Long id, @RequestParam Role role, RedirectAttributes f) {
    try {
      users.changeRole(id, role);
      f.addFlashAttribute(
          "message", "Роль обновлена. Новые права действуют со следующего запроса.");
    } catch (IllegalArgumentException e) {
      f.addFlashAttribute("error", e.getMessage());
    }
    return "redirect:/admin/users";
  }

  @GetMapping("/logs")
  public String logs(Model m) {
    var u = users.current();
    m.addAttribute(
        "actions",
        u.isAdmin()
            ? actions.findTop200ByOrderByIdDesc()
            : actions.findTop200ByActorOrderByIdDesc(u.getUsername()));
    return "logs";
  }
}
