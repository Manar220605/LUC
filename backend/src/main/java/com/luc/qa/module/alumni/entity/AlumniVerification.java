package com.luc.qa.module.alumni.entity;

import com.luc.qa.common.entity.AuditableEntity;
import com.luc.qa.module.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "alumni_verifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlumniVerification extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "linkedin_url", nullable = false, length = 500)
    private String linkedinUrl;

    @Column(name = "claimed_grad_year", nullable = false)
    private int claimedGradYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "claimed_faculty", nullable = false, length = 50)
    private Faculty claimedFaculty;

    @Enumerated(EnumType.STRING)
    @Column(name = "claimed_degree", nullable = false, length = 20)
    private Degree claimedDegree;

    @Column(name = "claimed_major", nullable = false, length = 100)
    private String claimedMajor;

    @Column(name = "claimed_position", length = 200)
    private String claimedPosition;

    @Column(name = "claimed_company", length = 200)
    private String claimedCompany;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private VerificationStatus status = VerificationStatus.PENDING;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;
}
