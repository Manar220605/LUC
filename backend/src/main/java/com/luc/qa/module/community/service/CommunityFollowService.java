package com.luc.qa.module.community.service;

import com.luc.qa.module.community.dto.CommunityFollowResponseDTO;
import com.luc.qa.module.community.dto.FollowStatusDTO;
import com.luc.qa.module.question.entity.Question;
import java.util.List;

public interface CommunityFollowService {

    FollowStatusDTO follow(String keycloakId, String communityPath);

    FollowStatusDTO unfollow(String keycloakId, String communityPath);

    FollowStatusDTO status(String keycloakId, String communityPath);

    List<CommunityFollowResponseDTO> listMine(String keycloakId);

    void notifyFollowers(Question question);
}
