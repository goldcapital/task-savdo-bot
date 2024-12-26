package org.programming.tasksavdobot.reposetory;
import org.programming.tasksavdobot.domen.AttachEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttachRepository extends JpaRepository<AttachEntity, String> {
    Optional<AttachEntity>findById(String attachId);
}
