package com.luc.qa.module.moderation.repository;

import com.luc.qa.module.moderation.entity.Report;
import com.luc.qa.module.moderation.entity.ReportStatus;
import com.luc.qa.module.moderation.entity.ReportTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;

public interface ReportRepository extends JpaRepository<Report, Long>, JpaSpecificationExecutor<Report> {

    boolean existsByReporterIdAndTargetTypeAndTargetIdAndStatus(
        Long reporterId,
        ReportTargetType targetType,
        Long targetId,
        ReportStatus status
    );

    @EntityGraph(attributePaths = {"reporter", "resolver"})
    java.util.Optional<Report> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"reporter", "resolver"})
    Page<Report> findAll(Specification<Report> spec, Pageable pageable);

    long countByStatus(ReportStatus status);
}
