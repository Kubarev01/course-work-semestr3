package ru.kubarev.releaseflow.config;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.boot.CommandLineRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.kubarev.releaseflow.domain.*;
import ru.kubarev.releaseflow.repository.*;
import java.util.*;import java.time.*;import java.math.*;
@Component @Profile("demo") @RequiredArgsConstructor public class DemoData implements CommandLineRunner {
 private final UserRepository users;private final ReleaseRepository releases;private final TaskRepository tasks;private final PasswordEncoder encoder;
 @Transactional public void run(String... args){
  if(users.count()>0)return;
  var admin=user("admin","Егор Кубарев",Role.ADMIN);var egor=user("egor","Егор · Разработчик",Role.USER);
  var anna=user("anna","Анна · Разработчик",Role.USER);user("viewer","Наблюдатель",Role.READ_ONLY);
  var r=release("v1.0 · Первый запуск","Авторизация, рабочая область и первый выпуск продукта",egor,LocalDate.now().plusDays(14));
  task("Разграничение доступа по ролям",r,egor,TaskStatus.DONE,Priority.HIGH,"3.00","12000",4);
  task("Доска задач и фильтры",r,egor,TaskStatus.IN_PROGRESS,Priority.HIGH,"4.00","10000",7);
  task("Интеграционные тесты сценариев",r,egor,TaskStatus.TODO,Priority.MEDIUM,"2.00","12000",10);
  task("Руководство пользователя",r,egor,TaskStatus.TODO,Priority.LOW,"1.00","8000",12);
  var r2=release("v1.1 · Аналитика","Расчёты и отчётность для команды",anna,LocalDate.now().plusDays(28));
  task("Калькулятор бюджета релиза",r2,anna,TaskStatus.IN_PROGRESS,Priority.HIGH,"3.00","11000",14);
  task("Проверка граничных значений",r2,anna,TaskStatus.TODO,Priority.MEDIUM,"2.00","11000",18);
  release("v2.0 · Интеграции","Планируемые внешние интеграции",admin,LocalDate.now().plusDays(60));
 }
 private AppUser user(String login,String name,Role role){AppUser u=new AppUser();u.setUsername(login);u.setDisplayName(name);u.setPassword(encoder.encode("ReleaseDemo!2026"));u.setRoles(role==Role.READ_ONLY?EnumSet.of(Role.READ_ONLY):EnumSet.of(Role.READ_ONLY,role));return users.save(u);}
 private Release release(String name,String description,AppUser owner,LocalDate date){Release r=new Release();r.setName(name);r.setDescription(description);r.setOwner(owner);r.setTargetDate(date);r.setStatus(ReleaseStatus.ACTIVE);return releases.save(r);}
 private void task(String title,Release r,AppUser u,TaskStatus status,Priority priority,String days,String rate,int offset){Task t=new Task();t.setTitle(title);t.setDescription("Учебная задача для демонстрации рабочего процесса ReleaseFlow.");t.setDays(new BigDecimal(days));t.setDailyRate(new BigDecimal(rate));t.setOwner(u);t.setRelease(r);t.setStatus(status);t.setPriority(priority);t.setDueDate(LocalDate.now().plusDays(offset));tasks.save(t);}
}
