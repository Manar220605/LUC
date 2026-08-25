package com.luc.qa.module.community.service;

import com.luc.qa.common.exception.CommunityNotFoundException;
import com.luc.qa.common.exception.UserNotFoundException;
import com.luc.qa.module.community.dto.CommunityFollowResponseDTO;
import com.luc.qa.module.community.dto.FollowStatusDTO;
import com.luc.qa.module.community.entity.Community;
import com.luc.qa.module.community.entity.CommunityFollow;
import com.luc.qa.module.community.mapper.CommunityMapper;
import com.luc.qa.module.community.repository.CommunityFollowRepository;
import com.luc.qa.module.community.repository.CommunityRepository;
import com.luc.qa.module.notification.service.NotificationService;
import com.luc.qa.module.question.entity.Question;
import com.luc.qa.module.user.entity.User;
import com.luc.qa.module.user.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommunityFollowServiceImpl implements CommunityFollowService {

    private final CommunityFollowRepository communityFollowRepository;
    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;
    private final CommunityMapper communityMapper;
    private final NotificationService notificationService;

    @Override
    public FollowStatusDTO follow(String keycloakId, String communityPath) {
        User user = findUser(keycloakId);
        Community community = findCommunity(communityPath);
        if (!communityFollowRepository.existsByUserIdAndCommunityId(user.getId(), community.getId())) {
            communityFollowRepository.save(CommunityFollow.builder()
                .user(user)
                .community(community)
                .build());
        }
        return statusFor(user.getId(), community.getId());
    }

    @Override
    public FollowStatusDTO unfollow(String keycloakId, String communityPath) {
        User user = findUser(keycloakId);
        Community community = findCommunity(communityPath);
        communityFollowRepository.deleteByUserIdAndCommunityId(user.getId(), community.getId());
        return statusFor(user.getId(), community.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public FollowStatusDTO status(String keycloakId, String communityPath) {
        User user = findUser(keycloakId);
        Community community = findCommunity(communityPath);
        return statusFor(user.getId(), community.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommunityFollowResponseDTO> listMine(String keycloakId) {
        User user = findUser(keycloakId);
        return communityFollowRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
            .map(follow -> CommunityFollowResponseDTO.builder()
                .community(communityMapper.toResponse(follow.getCommunity()))
                .followedAt(follow.getCreatedAt())
                .build())
            .toList();
    }

    @Override
    public void notifyFollowers(Question question) {
        User author = question.getAuthor();
        Community community = question.getCommunity();
        if (author == null || community == null) {
            return;
        }
        for (CommunityFollow follow : communityFollowRepository.findByCommunityId(community.getId())) {
            User follower = follow.getUser();
            if (follower == null || follower.isBanned()) {
                continue;
            }
            notificationService.notifyNewQuestionInCommunity(
                author,
                follower,
                question,
                question.isAnonymous()
            );
        }
    }

    private FollowStatusDTO statusFor(Long userId, Long communityId) {
        return FollowStatusDTO.builder()
            .following(communityFollowRepository.existsByUserIdAndCommunityId(userId, communityId))
            .followerCount(communityFollowRepository.countByCommunityId(communityId))
            .build();
    }

    private User findUser(String keycloakId) {
        return userRepository.findByKeycloakId(UUID.fromString(keycloakId))
            .orElseThrow(() -> new UserNotFoundException("Keycloak sub " + keycloakId));
    }

    private Community findCommunity(String communityPath) {
        String path = communityPath == null ? "" : communityPath.trim();
        return communityRepository.findByPath(path)
            .orElseThrow(() -> new CommunityNotFoundException(path));
    }
}
