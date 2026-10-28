package ru.kubarev.releaseflow.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "RELEASES")
public class Release {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Version private Long version;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(nullable = false, length = 1500)
  private String description = "";

  @Column(nullable = false)
  private LocalDate targetDate;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ReleaseStatus status = ReleaseStatus.PLANNED;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_id", nullable = false)
  private AppUser owner;
}
