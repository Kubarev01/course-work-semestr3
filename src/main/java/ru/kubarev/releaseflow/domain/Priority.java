package ru.kubarev.releaseflow.domain;
public enum Priority {
 LOW("Низкий"),
 MEDIUM("Средний"),
 HIGH("Высокий");
 private final String label;
 Priority(String label){this.label=label;}
 public String getLabel(){return label;}
}
