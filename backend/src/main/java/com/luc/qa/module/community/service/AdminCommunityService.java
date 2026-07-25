package com.luc.qa.module.community.service;

import com.luc.qa.common.exception.CommunityNotFoundException;
import com.luc.qa.common.exception.ConflictException;
import com.luc.qa.module.community.dto.CreateCommunityRequestDTO;
import com.luc.qa.module.community.dto.UpdateCommunityRequestDTO;
import com.luc.qa.module.community.entity.Community;
import com.luc.qa.module.community.repository.CommunityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminCommunityService {

    private final CommunityRepository communityRepository;
    private final CommunityPathService communityPathService;

    public Community create(CreateCommunityRequestDTO request) {
        String slug = request.getSlug().trim();
        communityPathService.validateSlug(slug);

        Community parent = null;
        if (request.getParentPath() != null && !request.getParentPath().isBlank()) {
            parent = communityRepository.findByPath(request.getParentPath().trim())
                .orElseThrow(() -> new CommunityNotFoundException(request.getParentPath()));
        }

        int depth = communityPathService.computeDepth(parent);
        communityPathService.validateDepth(depth);

        String path = communityPathService.computePath(parent, slug);
        if (communityRepository.findByPath(path).isPresent()) {
            throw new ConflictException("Community already exists at path " + path);
        }

        Community community = Community.builder()
            .slug(slug)
            .name(request.getName().trim())
            .description(request.getDescription())
            .parent(parent)
            .path(path)
            .depth(depth)
            .questionCount(0)
            .build();

        return communityRepository.save(community);
    }

    public Community update(Long id, UpdateCommunityRequestDTO request) {
        Community community = communityRepository.findById(id)
            .orElseThrow(() -> new CommunityNotFoundException(id));
        community.setName(request.getName().trim());
        community.setDescription(request.getDescription());
        return communityRepository.save(community);
    }

    public void delete(Long id) {
        Community community = communityRepository.findById(id)
            .orElseThrow(() -> new CommunityNotFoundException(id));

        if (communityRepository.existsByParentId(id)) {
            throw new ConflictException("Community has children and cannot be deleted");
        }
        if (community.getQuestionCount() > 0) {
            throw new ConflictException("Community has questions and cannot be deleted");
        }

        communityRepository.delete(community);
    }
}
