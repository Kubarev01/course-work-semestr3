package ru.kubarev.releaseflow.domain;
public enum TaskStatus {
 TODO("Запланирована"),
 IN_PROGRESS("В работе"),
 DONE("Готова");
 private final String label;
 TaskStatus(String label){this.label=label;}
 public String getLabel(){return label;}
}
