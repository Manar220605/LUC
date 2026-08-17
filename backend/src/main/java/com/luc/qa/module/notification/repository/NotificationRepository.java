package com.luc.qa.module.notification.repository;

import com.luc.qa.module.notification.entity.Notification;
import com.luc.qa.module.notification.entity.NotificationType;
import com.luc.qa.module.vote.entity.VoteTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @EntityGraph(attributePaths = {"actor", "question"})
    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);

    long countByRecipientIdAndReadAtIsNull(Long recipientId);

    @Modifying
    @Query("""
        UPDATE Notification n
        SET n.readAt = CURRENT_TIMESTAMP
        WHERE n.recipient.id = :recipientId AND n.readAt IS NULL
        """)
    int markAllReadForRecipient(@Param("recipientId") Long recipientId);

    boolean existsByRecipientIdAndTypeAndTargetTypeAndTargetId(
        Long recipientId,
        NotificationType type,
        VoteTargetType targetType,
        Long targetId
    );
}
