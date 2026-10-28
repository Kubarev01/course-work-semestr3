package ru.kubarev.releaseflow.service;

import java.math.*;
import java.time.*;
import java.util.List;
import org.springframework.stereotype.Service;
import ru.kubarev.releaseflow.domain.*;

@Service
public class CalculationService {
  public record Estimate(
      BigDecimal days,
      BigDecimal remainingDays,
      BigDecimal baseCost,
      BigDecimal budget,
      int progress,
      LocalDate finish) {}

  public Estimate calculate(List<Task> tasks, int people, BigDecimal reserve, LocalDate start) {
    if (people < 1
        || people > 100
        || reserve == null
        || reserve.signum() < 0
        || reserve.compareTo(new BigDecimal("100")) > 0
        || start == null) throw new IllegalArgumentException("Некорректные параметры расчёта");
    BigDecimal days = BigDecimal.ZERO, done = BigDecimal.ZERO, cost = BigDecimal.ZERO;
    for (Task t : tasks) {
      days = days.add(t.getDays());
      cost = cost.add(t.getCost());
      if (t.getStatus() == TaskStatus.DONE) done = done.add(t.getDays());
    }
    BigDecimal remaining = days.subtract(done);
    BigDecimal budget =
        cost.multiply(BigDecimal.ONE.add(reserve.divide(new BigDecimal("100"))))
            .setScale(2, RoundingMode.HALF_UP);
    int progress =
        days.signum() == 0
            ? 0
            : done.multiply(new BigDecimal("100")).divide(days, 0, RoundingMode.HALF_UP).intValue();
    int workdays =
        remaining.divide(BigDecimal.valueOf(people), 0, RoundingMode.CEILING).intValueExact();
    LocalDate finish = start;
    while (workdays > 0) {
      finish = finish.plusDays(1);
      if (finish.getDayOfWeek() != DayOfWeek.SATURDAY && finish.getDayOfWeek() != DayOfWeek.SUNDAY)
        workdays--;
    }
    return new Estimate(
        days, remaining, cost.setScale(2, RoundingMode.HALF_UP), budget, progress, finish);
  }
}
