package com.luc.qa.module.moderation.specification;

import com.luc.qa.module.moderation.dto.AdminReportFilterDTO;
import com.luc.qa.module.moderation.entity.Report;
import com.luc.qa.module.moderation.entity.ReportStatus;
import com.luc.qa.module.moderation.entity.ReportTargetType;
import org.springframework.data.jpa.domain.Specification;

public final class ReportSpecifications {

    private ReportSpecifications() {
    }

    public static Specification<Report> hasStatus(ReportStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Report> hasTargetType(ReportTargetType targetType) {
        return (root, query, cb) -> cb.equal(root.get("targetType"), targetType);
    }

    public static Specification<Report> fromFilter(AdminReportFilterDTO filter) {
        Specification<Report> spec = (root, query, cb) -> cb.conjunction();
        if (filter.getStatus() != null) {
            spec = spec.and(hasStatus(filter.getStatus()));
        }
        if (filter.getTargetType() != null) {
            spec = spec.and(hasTargetType(filter.getTargetType()));
        }
        return spec;
    }
}
