package com.luc.qa.module.user.specification;

import com.luc.qa.module.user.dto.AdminUserFilterDTO;
import com.luc.qa.module.user.entity.User;
import com.luc.qa.module.user.entity.UserRole;
import org.springframework.data.jpa.domain.Specification;

public final class UserSpecifications {

    private UserSpecifications() {
    }

    public static Specification<User> hasRole(UserRole role) {
        return (root, query, cb) -> cb.equal(root.get("role"), role);
    }

    public static Specification<User> isBanned(boolean banned) {
        return (root, query, cb) -> cb.equal(root.get("banned"), banned);
    }

    public static Specification<User> search(String search) {
        return (root, query, cb) -> {
            String like = "%" + search.toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("email")), like),
                cb.like(cb.lower(root.get("displayName")), like)
            );
        };
    }

    public static Specification<User> fromFilter(AdminUserFilterDTO filter) {
        Specification<User> spec = (root, query, cb) -> cb.conjunction();
        if (filter.getRole() != null) {
            spec = spec.and(hasRole(filter.getRole()));
        }
        if (filter.getIsBanned() != null) {
            spec = spec.and(isBanned(filter.getIsBanned()));
        }
        if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
            spec = spec.and(search(filter.getSearch().trim()));
        }
        return spec;
    }
}
