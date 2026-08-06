package com.luc.qa.module.alumni.entity;

import com.luc.qa.module.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "alumni_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class AlumniProfile implements Persistable<Long> {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Transient
    @Builder.Default
    private boolean newProfile = true;

    @Column(name = "grad_year", nullable = false)
    private int gradYear;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Faculty faculty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Degree degree;

    @Column(nullable = false, length = 100)
    private String major;

    @Column(name = "current_position", length = 200)
    private String currentPosition;

    @Column(name = "current_company", length = 200)
    private String currentCompany;

    @Column(name = "linkedin_url", length = 500)
    private String linkedinUrl;

    @Column(name = "is_public", nullable = false)
    @Builder.Default
    private boolean publicProfile = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    @Builder.Default
    private Long version = 0L;

    @Override
    public Long getId() {
        return userId;
    }

    @Override
    public boolean isNew() {
        return newProfile;
    }

    @PostLoad
    @PostPersist
    void markPersisted() {
        this.newProfile = false;
    }
}
