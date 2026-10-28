package ru.kubarev.releaseflow.domain;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
@Getter @Setter @NoArgsConstructor @Entity @Table(name="TASKS")
public class Task {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Version private Long version;
 @Column(nullable=false,length=120) private String title;
 @Column(nullable=false,length=2000) private String description="";
 @Column(nullable=false,precision=7,scale=2) private BigDecimal days;
 @Column(nullable=false,precision=12,scale=2) private BigDecimal dailyRate;
 @Column(nullable=false) private LocalDate dueDate;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private TaskStatus status=TaskStatus.TODO;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private Priority priority=Priority.MEDIUM;
 @ManyToOne(optional=false,fetch=FetchType.LAZY) @JoinColumn(name="release_id",nullable=false)
 private Release release;
 @ManyToOne(optional=false,fetch=FetchType.LAZY) @JoinColumn(name="owner_id",nullable=false)
 private AppUser owner;
 public BigDecimal getCost(){return days.multiply(dailyRate).setScale(2,java.math.RoundingMode.HALF_UP);}
 public boolean isOverdue(){return status!=TaskStatus.DONE&&dueDate.isBefore(LocalDate.now());}
}
