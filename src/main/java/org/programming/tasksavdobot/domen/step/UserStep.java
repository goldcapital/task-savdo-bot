package org.programming.tasksavdobot.domen.step;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_steps")
public class UserStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "step_name", nullable = false)
    private String stepName;

    @Column(name = "timestamp")
    private LocalDateTime timestamp=LocalDateTime.now();

    @Column(name = "additional")
    private String additionalData;

    private String typ;
}
