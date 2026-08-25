package com.luc.qa.module.registration.repository;

import com.luc.qa.module.registration.entity.StudentLookupChallenge;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentLookupChallengeRepository extends JpaRepository<StudentLookupChallenge, Long> {

    long countByRequesterIpAndCreatedAtAfter(String requesterIp, Instant after);

    long countByFileNumberAndCreatedAtAfter(String fileNumber, Instant after);

    Optional<StudentLookupChallenge> findFirstByFileNumberAndLookupTokenHashIsNullAndVerifiedAtIsNullOrderByCreatedAtDesc(
        String fileNumber
    );
}
