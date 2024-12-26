package org.programming.tasksavdobot.domen;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.programming.tasksavdobot.enums.Language;
import org.programming.tasksavdobot.enums.ProfileRole;
import org.programming.tasksavdobot.enums.Status;

import java.time.LocalDateTime;

@Getter
@Setter

@Entity
public class ProfileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String fullName;

    @Column
    private String type;

    @Column
    private String phone;

    @Column(unique = true)
    private Long chatId;

    @Column
    private String username;

    @Enumerated(EnumType.STRING)
    private ProfileRole role;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.ORDINAL)
    private Language language=Language.UZ;

    private LocalDateTime dateTime;

}
