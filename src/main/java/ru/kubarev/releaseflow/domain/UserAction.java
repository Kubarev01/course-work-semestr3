package ru.kubarev.releaseflow.domain;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
@Getter @Setter @NoArgsConstructor @Entity @Table(name="USER_ACTIONS",indexes=@Index(name="ix_actions_actor_date",columnList="actor,date_actions"))
public class UserAction {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="date_actions",nullable=false) private Instant dateActions;
 @Column(nullable=false,length=80) private String actor;
 @Column(nullable=false,length=40) private String action;
 @Column(nullable=false,length=500) private String description;
}
