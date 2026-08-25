package com.luc.qa.module.user.service;

import com.luc.qa.common.exception.ConflictException;
import com.luc.qa.common.exception.UserNotFoundException;
import com.luc.qa.common.keycloak.KeycloakAdminClient;
import com.luc.qa.module.user.dto.OnboardingRequestDTO;
import com.luc.qa.module.user.dto.UpdateProfileRequestDTO;
import com.luc.qa.module.user.entity.User;
import com.luc.qa.module.user.entity.UserRole;
import com.luc.qa.module.user.repository.UserRepository;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final KeycloakAdminClient keycloakAdminClient;
    private final AvatarStorage avatarStorage;

    @Override
    @Transactional(readOnly = true)
    public User getByKeycloakId(String keycloakId) {
        return userRepository.findByKeycloakId(UUID.fromString(keycloakId))
            .orElseThrow(() -> new UserNotFoundException("Keycloak sub " + keycloakId));
    }

    @Override
    public User updateProfile(String keycloakId, UpdateProfileRequestDTO request) {
        User user = getByKeycloakId(keycloakId);
        user.setDisplayName(request.getDisplayName().trim());
        user.setBio(blankToNull(request.getBio()));
        return userRepository.save(user);
    }

    @Override
    public User updateAvatar(String keycloakId, MultipartFile file) {
        User user = getByKeycloakId(keycloakId);
        String previous = user.getAvatarUrl();
        String storedUrl = avatarStorage.store(file);
        user.setAvatarUrl(storedUrl);
        User saved = userRepository.save(user);
        avatarStorage.deleteIfStored(previous);
        return saved;
    }

    @Override
    public User clearAvatar(String keycloakId) {
        User user = getByKeycloakId(keycloakId);
        String previous = user.getAvatarUrl();
        user.setAvatarUrl(null);
        User saved = userRepository.save(user);
        avatarStorage.deleteIfStored(previous);
        return saved;
    }

    @Override
    public User completeOnboarding(String keycloakId, OnboardingRequestDTO request) {
        User user = getByKeycloakId(keycloakId);
        String studentId = request.getStudentId().trim();

        if (userRepository.existsByStudentIdAndIdNot(studentId, user.getId())) {
            throw new ConflictException("Student ID already in use");
        }

        user.setStudentId(studentId);
        user.setVerifiedStudentAt(Instant.now());
        user.setRole(UserRole.STUDENT);
        User saved = userRepository.save(user);
        keycloakAdminClient.assignRealmRole(user.getKeycloakId(), UserRole.STUDENT.name());
        return saved;
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
