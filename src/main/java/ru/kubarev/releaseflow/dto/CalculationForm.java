package ru.kubarev.releaseflow.dto;
import lombok.Data;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
@Data public class CalculationForm {
 @NotNull private Long releaseId;
 @Min(1) @Max(100) private int people=1;
 @NotNull @DecimalMin("0") @DecimalMax("100") private BigDecimal reserve=new BigDecimal("15");
 @NotNull @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) private LocalDate startDate=LocalDate.now();
}
