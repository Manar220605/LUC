package com.luc.qa.module.user.service;

import com.luc.qa.module.user.entity.User;
import com.luc.qa.module.user.entity.UserRole;
import com.luc.qa.module.user.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class UserProvisioningService {

    private final UserRepository userRepository;
    private final PlatformTransactionManager transactionManager;

    public User provisionIfAbsent(Jwt jwt) {
        UUID keycloakId = UUID.fromString(jwt.getSubject());
        return userRepository.findByKeycloakId(keycloakId)
            .orElseGet(() -> insertOrGet(jwt, keycloakId));
    }

    /**
     * First authenticated request after LinkedIn/Keycloak signup often fans out into several
     * parallel API calls. Each hits this filter; without a race-safe insert the second insert
     * fails on users_keycloak_id_key and the page shows Internal Server Error.
     */
    private User insertOrGet(Jwt jwt, UUID keycloakId) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        try {
            User created = transactionTemplate.execute(status ->
                userRepository.findByKeycloakId(keycloakId)
                    .orElseGet(() -> userRepository.save(buildFromJwt(jwt, keycloakId)))
            );
            if (created == null) {
                throw new IllegalStateException("User provisioning returned null");
            }
            return created;
        } catch (DataIntegrityViolationException ex) {
            return userRepository.findByKeycloakId(keycloakId).orElseThrow(() -> ex);
        }
    }

    private User buildFromJwt(Jwt jwt, UUID keycloakId) {
        String email = jwt.getClaimAsString("email");
        if (email == null || email.isBlank()) {
            email = keycloakId + "@luc.local";
        }

        return User.builder()
            .keycloakId(keycloakId)
            .email(email)
            .displayName(resolveDisplayName(jwt))
            .role(UserRole.MEMBER)
            .banned(false)
            .build();
    }

    private String resolveDisplayName(Jwt jwt) {
        String name = jwt.getClaimAsString("name");
        if (name != null && !name.isBlank()) {
            return name.trim();
        }

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
