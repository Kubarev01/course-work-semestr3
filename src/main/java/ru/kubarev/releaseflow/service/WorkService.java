package ru.kubarev.releaseflow.service;

import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.kubarev.releaseflow.domain.*;
import ru.kubarev.releaseflow.dto.*;
import ru.kubarev.releaseflow.repository.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkService {
  private final ReleaseRepository releases;
  private final TaskRepository tasks;
  private final UserService users;
  private final AccessService access;
  private final AuditService audit;

  public List<Release> releases() {
    return access.ownOnly()
        ? releases.findByOwnerUsernameOrderByTargetDateAsc(users.current().getUsername())
        : releases.findAllByOrderByTargetDateAsc();
  }

  public List<Release> releasesForTask(Long taskId) {
    Long ownerId = taskId == null ? users.current().getId() : task(taskId).getOwner().getId();
    return releases().stream().filter(r -> r.getOwner().getId().equals(ownerId)).toList();
  }

  public List<Task> tasks(String q, TaskStatus status, Long releaseId) {
    var list =
        access.ownOnly()
            ? tasks.findByOwnerUsernameOrderByIdDesc(users.current().getUsername())
            : tasks.findAllByOrderByIdDesc();
    String query = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
    return list.stream()
        .filter(
            t ->
                query.isEmpty()
                    || t.getTitle().toLowerCase(Locale.ROOT).contains(query)
                    || t.getDescription().toLowerCase(Locale.ROOT).contains(query))
        .filter(t -> status == null || t.getStatus() == status)
        .filter(t -> releaseId == null || t.getRelease().getId().equals(releaseId))
        .toList();
  }

  public Release release(Long id) {
    Release r =
        releases.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    access.read(r.getOwner());
    return r;
  }

  public Task task(Long id) {
    Task t =
        tasks.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    access.read(t.getOwner());
    return t;
  }

  private void version(Long submitted, Long stored) {
    if (!Objects.equals(submitted, stored))
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Запись уже изменена. Обновите страницу.");
  }

  @Transactional
  public Release saveRelease(Long id, ReleaseForm f) {
    Release r;
    if (id == null) {
      access.create();
      r = new Release();
      r.setOwner(users.current());
    } else {
      r = release(id);
      access.write(r.getOwner());
      version(f.getVersion(), r.getVersion());
    }
    r.setName(f.getName().trim());
    r.setDescription(f.getDescription().trim());
    r.setTargetDate(f.getTargetDate());
    r.setStatus(f.getStatus());
    releases.saveAndFlush(r);
    audit.record(
        users.current().getUsername(),
        id == null ? "RELEASE_CREATE" : "RELEASE_UPDATE",
        "Релиз #" + r.getId());
    return r;
  }

  @Transactional
  public void deleteRelease(Long id) {
    Release r = release(id);
    access.write(r.getOwner());
    if (tasks.existsByReleaseId(id))
      throw new IllegalArgumentException("Сначала удалите или перенесите задачи этого релиза");
    releases.delete(r);
    audit.record(users.current().getUsername(), "RELEASE_DELETE", "Релиз #" + id);
  }

  @Transactional
  public Task saveTask(Long id, TaskForm f) {
    Task t;
    if (id == null) {
      access.create();
      t = new Task();
      t.setOwner(users.current());
    } else {
      t = task(id);
      access.write(t.getOwner());
      version(f.getVersion(), t.getVersion());
    }
    Release r = release(f.getReleaseId());
    access.write(r.getOwner());
    if (!t.getOwner().getId().equals(r.getOwner().getId()))
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Владелец задачи и релиза должен совпадать");
    t.setTitle(f.getTitle().trim());
    t.setDescription(f.getDescription().trim());
    t.setDays(f.getDays());
    t.setDailyRate(f.getDailyRate());
    t.setDueDate(f.getDueDate());
    t.setPriority(f.getPriority());
    t.setStatus(f.getStatus());
    t.setRelease(r);
    tasks.saveAndFlush(t);
    audit.record(
        users.current().getUsername(),
        id == null ? "TASK_CREATE" : "TASK_UPDATE",
        "Задача #" + t.getId());
    return t;
  }

  @Transactional
  public void deleteTask(Long id) {
    Task t = task(id);
    access.write(t.getOwner());
    tasks.delete(t);
    audit.record(users.current().getUsername(), "TASK_DELETE", "Задача #" + id);
  }

  public ReleaseForm releaseForm(Long id) {
    Release r = release(id);
    access.write(r.getOwner());
    ReleaseForm f = new ReleaseForm();
    f.setVersion(r.getVersion());
    f.setName(r.getName());
    f.setDescription(r.getDescription());
    f.setTargetDate(r.getTargetDate());
    f.setStatus(r.getStatus());
    return f;
  }

  public TaskForm taskForm(Long id) {
    Task t = task(id);
    access.write(t.getOwner());
    TaskForm f = new TaskForm();
    f.setVersion(t.getVersion());
    f.setTitle(t.getTitle());
    f.setDescription(t.getDescription());
    f.setDays(t.getDays());
    f.setDailyRate(t.getDailyRate());
    f.setDueDate(t.getDueDate());
    f.setStatus(t.getStatus());
    f.setPriority(t.getPriority());
    f.setReleaseId(t.getRelease().getId());
    return f;
  }
}
