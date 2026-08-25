package com.luc.qa.module.mentorship.repository;

import com.luc.qa.module.mentorship.entity.MentorshipRequest;
import com.luc.qa.module.mentorship.entity.MentorshipRequestStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MentorshipRequestRepository extends JpaRepository<MentorshipRequest, Long> {

    @EntityGraph(attributePaths = {"student", "alumni"})
    List<MentorshipRequest> findByAlumniIdOrderByCreatedAtDesc(Long alumniId);

    @EntityGraph(attributePaths = {"student", "alumni"})
    List<MentorshipRequest> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    @EntityGraph(attributePaths = {"student", "alumni"})
    Optional<MentorshipRequest> findTopByStudentIdAndAlumniIdOrderByCreatedAtDesc(
        Long studentId,
        Long alumniId
    );

    boolean existsByStudentIdAndAlumniIdAndStatus(
        Long studentId,
        Long alumniId,
        MentorshipRequestStatus status
    );

    @EntityGraph(attributePaths = {"student", "alumni"})
    Optional<MentorshipRequest> findByIdAndAlumniId(Long id, Long alumniId);
}
