package com.luc.qa.module.alumni.repository;

import com.luc.qa.module.alumni.entity.AlumniProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AlumniProfileRepository
    extends JpaRepository<AlumniProfile, Long>, JpaSpecificationExecutor<AlumniProfile> {

    @Override
    @EntityGraph(attributePaths = "user")
    Page<AlumniProfile> findAll(Specification<AlumniProfile> spec, Pageable pageable);
}
