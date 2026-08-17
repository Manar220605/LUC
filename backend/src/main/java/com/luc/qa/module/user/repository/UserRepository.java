package com.luc.qa.module.user.repository;

import com.luc.qa.module.user.entity.User;
import com.luc.qa.module.user.entity.UserRole;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByKeycloakId(UUID keycloakId);

    Optional<User> findByDisplayNameIgnoreCase(String displayName);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByStudentId(String studentId);

    boolean existsByStudentIdAndIdNot(String studentId, Long id);

    long countByRole(UserRole role);
}
