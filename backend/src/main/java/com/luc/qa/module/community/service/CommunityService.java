package com.luc.qa.module.community.service;

import com.luc.qa.common.exception.CommunityNotFoundException;
import com.luc.qa.module.community.dto.CommunityTreeNodeDTO;
import com.luc.qa.module.community.entity.Community;
import com.luc.qa.module.community.mapper.CommunityMapper;
import com.luc.qa.module.community.repository.CommunityRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final CommunityMapper communityMapper;

    public List<CommunityTreeNodeDTO> getTree() {
        List<Community> all = communityRepository.findAllByOrderByPathAsc();
        return buildTree(all);
    }

    public Community findByPath(String path) {
        return communityRepository.findByPath(path)
            .orElseThrow(() -> new CommunityNotFoundException(path));
    }

    public List<Community> findChildren(String path) {
        Community parent = findByPath(path);
        return communityRepository.findByParentIdOrderByPathAsc(parent.getId());
    }

    private List<CommunityTreeNodeDTO> buildTree(List<Community> communities) {
        Map<Long, CommunityTreeNodeDTO> nodesById = new HashMap<>();
        for (Community community : communities) {
            nodesById.put(community.getId(), communityMapper.toTreeNode(community));
        }

        List<CommunityTreeNodeDTO> roots = new ArrayList<>();
        for (Community community : communities) {
            CommunityTreeNodeDTO node = nodesById.get(community.getId());
            if (community.getParent() == null) {
                roots.add(node);
            } else {
                CommunityTreeNodeDTO parent = nodesById.get(community.getParent().getId());
                if (parent != null) {
                    parent.getChildren().add(node);
                }
            }
        }

        sortTree(roots);
        return roots;
    }

    private void sortTree(List<CommunityTreeNodeDTO> nodes) {
        nodes.sort(Comparator.comparing(CommunityTreeNodeDTO::getPath));
        for (CommunityTreeNodeDTO node : nodes) {
            sortTree(node.getChildren());
        }
    }
}
