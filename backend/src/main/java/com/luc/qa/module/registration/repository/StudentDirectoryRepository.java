package com.luc.qa.module.registration.repository;

import com.luc.qa.module.registration.entity.StudentDirectory;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentDirectoryRepository extends JpaRepository<StudentDirectory, String> {

    Optional<StudentDirectory> findByFileNumberAndEmailIgnoreCase(String fileNumber, String email);
}
