package ru.kubarev.releaseflow.domain;

public enum Role {
  READ_ONLY("Наблюдатель"),
  USER("Участник"),
  ADMIN("Администратор");
  private final String label;

  Role(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
