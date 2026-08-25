package com.luc.qa.module.community.repository;

import com.luc.qa.module.community.entity.CommunityFollow;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityFollowRepository extends JpaRepository<CommunityFollow, Long> {

    boolean existsByUserIdAndCommunityId(Long userId, Long communityId);

    void deleteByUserIdAndCommunityId(Long userId, Long communityId);

    long countByCommunityId(Long communityId);

    @EntityGraph(attributePaths = {"community", "community.parent"})
    List<CommunityFollow> findByUserIdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = "user")
    List<CommunityFollow> findByCommunityId(Long communityId);
}
