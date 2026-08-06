package com.luc.qa.module.alumni.repository;

import com.luc.qa.module.alumni.entity.AlumniProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlumniProfileRepository extends JpaRepository<AlumniProfile, Long> {
}
