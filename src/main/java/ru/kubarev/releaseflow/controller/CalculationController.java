package ru.kubarev.releaseflow.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.kubarev.releaseflow.dto.CalculationForm;
import ru.kubarev.releaseflow.service.*;

@Controller
@RequiredArgsConstructor
public class CalculationController {
  private final WorkService work;
  private final CalculationService calc;
  private final AuditService audit;
  private final UserService users;

  @GetMapping("/estimate")
  public String form(Model m) {
    m.addAttribute("form", new CalculationForm());
    m.addAttribute("releases", work.releases());
    return "estimate";
  }

  @PostMapping("/estimate")
  public String calculate(
      @Valid @ModelAttribute("form") CalculationForm f, BindingResult errors, Model m) {
    m.addAttribute("releases", work.releases());
    if (errors.hasErrors()) return "estimate";
    var release = work.release(f.getReleaseId());
    var tasks = work.tasks(null, null, f.getReleaseId());
    m.addAttribute(
        "estimate", calc.calculate(tasks, f.getPeople(), f.getReserve(), f.getStartDate()));
    m.addAttribute("release", release);
    audit.record(users.current().getUsername(), "CALCULATE", "Расчёт релиза #" + release.getId());
    return "estimate";
  }
}
