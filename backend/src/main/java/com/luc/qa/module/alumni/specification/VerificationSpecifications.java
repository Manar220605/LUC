package com.luc.qa.module.alumni.specification;

import com.luc.qa.module.alumni.dto.AdminVerificationFilterDTO;
import com.luc.qa.module.alumni.entity.AlumniVerification;
import com.luc.qa.module.alumni.entity.VerificationStatus;
import org.springframework.data.jpa.domain.Specification;

public final class VerificationSpecifications {

    private VerificationSpecifications() {
    }

    public static Specification<AlumniVerification> hasStatus(VerificationStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<AlumniVerification> fromFilter(AdminVerificationFilterDTO filter) {
        Specification<AlumniVerification> spec = (root, query, cb) -> cb.conjunction();
        if (filter.getStatus() != null) {
            spec = spec.and(hasStatus(filter.getStatus()));
        }
        return spec;
    }
}
