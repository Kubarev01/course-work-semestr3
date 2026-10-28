package ru.kubarev.releaseflow.dto;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import lombok.Data;
import java.time.LocalDate;
import ru.kubarev.releaseflow.domain.ReleaseStatus;
@Data public class ReleaseForm {
 private Long version;
 @NotBlank(message="Введите название") @Size(max=100) private String name;
 @NotNull @Size(max=1500) private String description="";
 @NotNull(message="Укажите дату") @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) private LocalDate targetDate;
 @NotNull private ReleaseStatus status=ReleaseStatus.PLANNED;
}
