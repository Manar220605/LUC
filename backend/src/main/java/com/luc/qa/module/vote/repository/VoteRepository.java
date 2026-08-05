package com.luc.qa.module.vote.repository;

import com.luc.qa.module.vote.entity.Vote;
import com.luc.qa.module.vote.entity.VoteTargetType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteRepository extends JpaRepository<Vote, Long> {

    Optional<Vote> findByVoterIdAndTargetTypeAndTargetId(
        Long voterId,
        VoteTargetType targetType,
        Long targetId
    );

    List<Vote> findByVoterIdAndTargetTypeAndTargetIdIn(
        Long voterId,
        VoteTargetType targetType,
        Collection<Long> targetIds
    );
}
