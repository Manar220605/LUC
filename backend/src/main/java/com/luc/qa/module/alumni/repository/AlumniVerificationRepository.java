package com.luc.qa.module.alumni.repository;

import com.luc.qa.module.alumni.entity.AlumniVerification;
import com.luc.qa.module.alumni.entity.VerificationStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AlumniVerificationRepository extends JpaRepository<AlumniVerification, Long>,
    JpaSpecificationExecutor<AlumniVerification> {

    boolean existsByUserIdAndStatus(Long userId, VerificationStatus status);

    Optional<AlumniVerification> findTopByUserIdOrderBySubmittedAtDesc(Long userId);

    long countByStatus(VerificationStatus status);
}
