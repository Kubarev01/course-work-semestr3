package ru.kubarev.releaseflow;

import static org.junit.jupiter.api.Assertions.*;

import java.math.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import ru.kubarev.releaseflow.domain.*;
import ru.kubarev.releaseflow.service.CalculationService;

class CalculationTest {
  CalculationService service = new CalculationService();

  Task t(String days, String rate, TaskStatus state) {
    Task t = new Task();
    t.setDays(new BigDecimal(days));
    t.setDailyRate(new BigDecimal(rate));
    t.setStatus(state);
    return t;
  }

  @Test
  void exactMoneyAndWeightedProgress() {
    var r =
        service.calculate(
            List.of(
                t("3", "12000", TaskStatus.DONE),
                t("4", "10000", TaskStatus.IN_PROGRESS),
                t("2", "12000", TaskStatus.TODO),
                t("1", "8000", TaskStatus.TODO)),
            2,
            new BigDecimal("15"),
            LocalDate.of(2026, 10, 28));
    assertEquals(new BigDecimal("108000.00"), r.baseCost());
    assertEquals(new BigDecimal("124200.00"), r.budget());
    assertEquals(30, r.progress());
    assertEquals(LocalDate.of(2026, 11, 3), r.finish());
  }

  @Test
  void weekendAndRounding() {
    var r =
        service.calculate(
            List.of(t("0.25", "100.01", TaskStatus.TODO)),
            1,
            BigDecimal.ZERO,
            LocalDate.of(2026, 10, 30));
    assertEquals(new BigDecimal("25.00"), r.budget());
    assertEquals(LocalDate.of(2026, 11, 2), r.finish());
  }

  @Test
  void emptyRelease() {
    var r = service.calculate(List.of(), 1, BigDecimal.ZERO, LocalDate.of(2026, 10, 28));
    assertEquals(0, r.progress());
    assertEquals(LocalDate.of(2026, 10, 28), r.finish());
    assertEquals(new BigDecimal("0.00"), r.budget());
  }

  @Test
  void invalidParameters() {
    assertThrows(
        IllegalArgumentException.class,
        () -> service.calculate(List.of(), 0, BigDecimal.ZERO, LocalDate.now()));
    assertThrows(
        IllegalArgumentException.class,
        () -> service.calculate(List.of(), 1, new BigDecimal("101"), LocalDate.now()));
  }
}
