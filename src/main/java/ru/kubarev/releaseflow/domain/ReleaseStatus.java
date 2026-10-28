package ru.kubarev.releaseflow.domain;
public enum ReleaseStatus {
 PLANNED("Запланирован"),
 ACTIVE("В работе"),
 RELEASED("Выпущен");
 private final String label;
 ReleaseStatus(String label){this.label=label;}
 public String getLabel(){return label;}
}
