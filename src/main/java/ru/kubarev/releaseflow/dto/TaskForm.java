package ru.kubarev.releaseflow.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import ru.kubarev.releaseflow.domain.*;

@Data
public class TaskForm {
  private Long version;

  @NotBlank(message = "Введите название")
  @Size(max = 120)
  private String title;

  @NotNull
  @Size(max = 2000)
  private String description = "";

  @NotNull
  @DecimalMin("0.25")
  @DecimalMax("365")
  @Digits(integer = 3, fraction = 2)
  private BigDecimal days;

  @NotNull
  @DecimalMin("0")
  @DecimalMax("1000000")
  @Digits(integer = 7, fraction = 2)
  private BigDecimal dailyRate;

  @NotNull
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate dueDate;

  @NotNull private TaskStatus status = TaskStatus.TODO;
  @NotNull private Priority priority = Priority.MEDIUM;

  @NotNull(message = "Выберите релиз")
  private Long releaseId;
}
