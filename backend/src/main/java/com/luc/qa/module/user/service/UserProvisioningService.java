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
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProvisioningService {

    private final UserRepository userRepository;

    @Transactional
    public User provisionIfAbsent(Jwt jwt) {
        UUID keycloakId = UUID.fromString(jwt.getSubject());
        return userRepository.findByKeycloakId(keycloakId)
            .orElseGet(() -> createFromJwt(jwt, keycloakId));
    }

    private User createFromJwt(Jwt jwt, UUID keycloakId) {
        String email = jwt.getClaimAsString("email");
        if (email == null || email.isBlank()) {
            email = keycloakId + "@luc.local";
        }

        User user = User.builder()
            .keycloakId(keycloakId)
            .email(email)
            .displayName(resolveDisplayName(jwt))
            .role(UserRole.MEMBER)
            .banned(false)
            .build();

        return userRepository.save(user);
    }

    private String resolveDisplayName(Jwt jwt) {
        String preferredUsername = jwt.getClaimAsString("preferred_username");
        if (preferredUsername != null && !preferredUsername.isBlank()) {
            return preferredUsername;
        }

        String givenName = jwt.getClaimAsString("given_name");
        String familyName = jwt.getClaimAsString("family_name");
        if (givenName != null || familyName != null) {
            return ((givenName != null ? givenName : "") + " " + (familyName != null ? familyName : "")).trim();
        }

        String email = jwt.getClaimAsString("email");
        if (email != null && email.contains("@")) {
            return email.substring(0, email.indexOf('@'));
        }

        return jwt.getSubject();
    }
}
