package com.luc.qa.module.community.repository;

import com.luc.qa.module.community.entity.Community;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommunityRepository extends JpaRepository<Community, Long> {

    @EntityGraph(attributePaths = "parent")
    Optional<Community> findByPath(String path);

    @EntityGraph(attributePaths = "parent")
    List<Community> findByParentIdOrderByPathAsc(Long parentId);

    List<Community> findByParentIsNullOrderByPathAsc();

    boolean existsByParentId(Long parentId);

    @EntityGraph(attributePaths = "parent")
    List<Community> findAllByOrderByPathAsc();

    @Modifying
    @Query("UPDATE Community c SET c.questionCount = c.questionCount + 1 WHERE c.id = :id")
    void incrementQuestionCount(@Param("id") Long id);
}
