package com.luc.qa.module.user.entity;

import com.luc.qa.common.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends AuditableEntity {

    @Column(name = "keycloak_id", nullable = false, unique = true)
    private UUID keycloakId;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(name = "student_id", unique = true, length = 20)
    private String studentId;

    @Column(name = "verified_student_at")
    private Instant verifiedStudentAt;

    @Column(name = "verified_alumni_at")
    private Instant verifiedAlumniAt;

    @Column(name = "is_banned", nullable = false)
    private boolean banned;

    @Column(name = "ban_reason", columnDefinition = "TEXT")
    private String banReason;
}
