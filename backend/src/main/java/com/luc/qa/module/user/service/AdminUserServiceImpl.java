package com.luc.qa.module.user.service;

import com.luc.qa.common.exception.UserNotFoundException;
import com.luc.qa.common.keycloak.KeycloakAdminClient;
import com.luc.qa.common.pagination.PageResponseDTO;
import com.luc.qa.module.user.dto.AdminUserFilterDTO;
import com.luc.qa.module.user.entity.User;
import com.luc.qa.module.user.repository.UserRepository;
import com.luc.qa.module.user.specification.UserSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final KeycloakAdminClient keycloakAdminClient;

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<User> listUsers(AdminUserFilterDTO filter) {
        Specification<User> spec = UserSpecifications.fromFilter(filter);
        PageRequest pageable = PageRequest.of(filter.getPage(), filter.getSize());
        Page<User> page = userRepository.findAll(spec, pageable);
        return PageResponseDTO.<User>builder()
            .content(page.getContent())
            .page(page.getNumber())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .build();
    }

    @Override
    public User banUser(Long id, String reason) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
        user.setBanned(true);
        user.setBanReason(reason);
        User saved = userRepository.save(user);
        keycloakAdminClient.disableUser(user.getKeycloakId());
        return saved;
    }

    @Override
    public User unbanUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
        user.setBanned(false);
        user.setBanReason(null);
        User saved = userRepository.save(user);
        keycloakAdminClient.enableUser(user.getKeycloakId());
        return saved;
    }
}
