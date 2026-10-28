package ru.kubarev.releaseflow.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.kubarev.releaseflow.domain.*;
import ru.kubarev.releaseflow.dto.*;
import ru.kubarev.releaseflow.service.*;

@Controller
@RequiredArgsConstructor
public class WorkController {
  private final WorkService work;
  private final AccessService access;
  private final AuditService audit;
  private final UserService users;

  @GetMapping("/dashboard")
  public String dashboard(Model m) {
    var tasks = work.tasks(null, null, null);
    m.addAttribute("tasks", tasks);
    m.addAttribute("releases", work.releases());
    m.addAttribute("done", tasks.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count());
    m.addAttribute("overdue", tasks.stream().filter(Task::isOverdue).count());
    return "dashboard";
  }

  @GetMapping("/tasks")
  public String tasks(
      @RequestParam(defaultValue = "") String q,
      @RequestParam(required = false) TaskStatus status,
      @RequestParam(required = false) Long releaseId,
      Model m) {
    m.addAttribute("tasks", work.tasks(q, status, releaseId));
    m.addAttribute("releases", work.releases());
    m.addAttribute("q", q);
    m.addAttribute("selectedStatus", status);
    m.addAttribute("selectedRelease", releaseId);
    audit.record(users.current().getUsername(), "TASK_LIST", "Просмотр списка задач");
    return "tasks";
  }

  @GetMapping("/board")
  public String board(Model m) {
    m.addAttribute("tasks", work.tasks(null, null, null));
    return "board";
  }

  @GetMapping("/releases")
  public String releases(Model m) {
    m.addAttribute("releases", work.releases());
    audit.record(users.current().getUsername(), "RELEASE_LIST", "Просмотр релизов");
    return "releases";
  }

  private String taskForm(Model m) {
    m.addAttribute("releases", work.releases());
    return "task-form";
  }

  @GetMapping("/tasks/new")
  public String newTask(Model m) {
    access.create();
    m.addAttribute("form", new TaskForm());
    return taskForm(m);
  }

  @GetMapping("/tasks/{id}/edit")
  public String editTask(@PathVariable Long id, Model m) {
    m.addAttribute("form", work.taskForm(id));
    m.addAttribute("id", id);
    return taskForm(m);
  }

  @PostMapping({"/tasks", "/tasks/{id}"})
  public String saveTask(
      @PathVariable(required = false) Long id,
      @Valid @ModelAttribute("form") TaskForm form,
      BindingResult errors,
      Model m,
      RedirectAttributes flash) {
    access.create();
    if (id != null) work.taskForm(id);
    m.addAttribute("id", id);
    if (errors.hasErrors()) return taskForm(m);
    work.saveTask(id, form);
    flash.addFlashAttribute("message", "Задача сохранена");
    return "redirect:/tasks";
  }

  @PostMapping("/tasks/{id}/delete")
  public String deleteTask(@PathVariable Long id, RedirectAttributes f) {
    work.deleteTask(id);
    f.addFlashAttribute("message", "Задача удалена");
    return "redirect:/tasks";
  }

  @GetMapping("/releases/new")
  public String newRelease(Model m) {
    access.create();
    m.addAttribute("form", new ReleaseForm());
    return "release-form";
  }

  @GetMapping("/releases/{id}/edit")
  public String editRelease(@PathVariable Long id, Model m) {
    m.addAttribute("form", work.releaseForm(id));
    m.addAttribute("id", id);
    return "release-form";
  }

  @PostMapping({"/releases", "/releases/{id}"})
  public String saveRelease(
      @PathVariable(required = false) Long id,
      @Valid @ModelAttribute("form") ReleaseForm form,
      BindingResult errors,
      Model m,
      RedirectAttributes f) {
    access.create();
    if (id != null) work.releaseForm(id);
    m.addAttribute("id", id);
    if (errors.hasErrors()) return "release-form";
    work.saveRelease(id, form);
    f.addFlashAttribute("message", "Релиз сохранён");
    return "redirect:/releases";
  }

  @PostMapping("/releases/{id}/delete")
  public String deleteRelease(@PathVariable Long id, RedirectAttributes f) {
    try {
      work.deleteRelease(id);
      f.addFlashAttribute("message", "Релиз удалён");
    } catch (IllegalArgumentException e) {
      f.addFlashAttribute("error", e.getMessage());
    }
    return "redirect:/releases";
  }
}
