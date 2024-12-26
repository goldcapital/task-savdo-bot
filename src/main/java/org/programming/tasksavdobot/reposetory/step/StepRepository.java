package org.programming.tasksavdobot.reposetory.step;

import org.programming.tasksavdobot.domen.step.UserStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StepRepository extends JpaRepository<UserStep,Long> {

@Query("from UserStep  where userId=?1")
    UserStep getByUserId(Long id);
}
