package org.programming.tasksavdobot.reposetory;

import org.programming.tasksavdobot.domen.ProfileEntity;
import org.programming.tasksavdobot.enums.ProfileRole;
import org.programming.tasksavdobot.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ProfileRepository extends JpaRepository<ProfileEntity, Long> {
    Optional<ProfileEntity> findByChatId(Long chatId);

    boolean existsByChatIdAndRole(Long userId, ProfileRole profileRole);

    List<ProfileEntity> findByRoleAndStatus(ProfileRole profileRole, Status status);

    Optional<ProfileEntity> getLanguageByChatId(Long chatId);

    @Transactional
    @Modifying
    @Query("UPDATE ProfileEntity p SET p.language = :language WHERE p.chatId = :chatId")
    void updateLanguageByChatId(@Param("chatId") Long chatId, @Param("language") String language);

    @Query("select p.username from ProfileEntity p where p.chatId =?1")
    String findByProfileID(Long profileId);
}
