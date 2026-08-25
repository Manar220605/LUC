package com.luc.qa.module.alumni.specification;

import com.luc.qa.module.alumni.dto.AlumniDirectoryFilterDTO;
import com.luc.qa.module.alumni.entity.AlumniProfile;
import com.luc.qa.module.alumni.entity.Faculty;
import com.luc.qa.module.user.entity.User;
import com.luc.qa.module.user.entity.UserRole;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class AlumniDirectorySpecifications {

    private AlumniDirectorySpecifications() {
    }

    public static Specification<AlumniProfile> fromFilter(AlumniDirectoryFilterDTO filter) {
        return (root, query, cb) -> {
            Join<AlumniProfile, User> user = userJoin(root);
            var predicate = cb.and(
                cb.isTrue(root.get("publicProfile")),
                cb.equal(user.get("role"), UserRole.ALUMNI),
                cb.isFalse(user.get("banned"))
            );
            if (filter.getGradYear() != null) {
                predicate = cb.and(predicate, cb.equal(root.get("gradYear"), filter.getGradYear()));
            }
            Faculty faculty = parseFaculty(filter.getFaculty());
            if (faculty != null) {
                predicate = cb.and(predicate, cb.equal(root.get("faculty"), faculty));
            }
            if (StringUtils.hasText(filter.getCompany())) {
                String like = "%" + filter.getCompany().trim().toLowerCase() + "%";
                predicate = cb.and(predicate, cb.like(cb.lower(root.get("currentCompany")), like));
            }
            if (StringUtils.hasText(filter.getQ())) {
                String like = "%" + filter.getQ().trim().toLowerCase() + "%";
                predicate = cb.and(
                    predicate,
                    cb.or(
                        cb.like(cb.lower(user.get("displayName")), like),
                        cb.like(cb.lower(root.get("currentCompany")), like),
                        cb.like(cb.lower(root.get("currentPosition")), like),
                        cb.like(cb.lower(root.get("major")), like)
                    )
                );
            }
            return predicate;
        };
    }

    @SuppressWarnings("unchecked")
    private static Join<AlumniProfile, User> userJoin(Root<AlumniProfile> root) {
        return root.getJoins().stream()
            .filter(join -> join.getAttribute().getName().equals("user"))
            .map(join -> (Join<AlumniProfile, User>) join)
            .findFirst()
            .orElseGet(() -> root.join("user", JoinType.INNER));
    }

    private static Faculty parseFaculty(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Faculty.valueOf(value.trim());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
