package com.luc.qa.common.keycloak;

import com.luc.qa.common.config.KeycloakConfig;
import com.luc.qa.common.exception.ConflictException;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
@RequiredArgsConstructor
@Slf4j
public class KeycloakAdminClient {

    private final KeycloakConfig keycloakConfig;
    private volatile Keycloak keycloak;

    public void assignRealmRole(UUID keycloakUserId, String roleName) {
        RealmResource realm = realm();
        RoleRepresentation role = realm.roles().get(roleName).toRepresentation();
        UserResource user = realm.users().get(keycloakUserId.toString());
        user.roles().realmLevel().add(List.of(role));
    }

    public void removeRealmRole(UUID keycloakUserId, String roleName) {
        RealmResource realm = realm();
        RoleRepresentation role = realm.roles().get(roleName).toRepresentation();
        UserResource user = realm.users().get(keycloakUserId.toString());
        user.roles().realmLevel().remove(List.of(role));
    }

    public void disableUser(UUID keycloakUserId) {
        updateEnabled(keycloakUserId, false);
    }

    public void enableUser(UUID keycloakUserId) {
        updateEnabled(keycloakUserId, true);
    }

    public UUID createUser(String email, String username, String firstName, String lastName) {
        UserRepresentation user = new UserRepresentation();
        user.setEnabled(true);
        user.setEmail(email);
        user.setEmailVerified(true);
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);

        try (Response response = realm().users().create(user)) {
            if (response.getStatus() == 409) {
                throw new ConflictException("An account with this email already exists");
            }
            if (response.getStatus() >= 400) {
                throw new IllegalStateException("Failed to create Keycloak user: HTTP " + response.getStatus());
            }
            String location = response.getLocation().getPath();
            String id = location.substring(location.lastIndexOf('/') + 1);
            return UUID.fromString(id);
        }
    }

    public void setPassword(UUID keycloakUserId, String password) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);
        realm().users().get(keycloakUserId.toString()).resetPassword(credential);
    }

    /**
     * Used for forgot-password from Keycloak itself, not for student sign-up.
     */
    public void sendPasswordSetupEmail(
        UUID keycloakUserId,
        String clientId,
        String redirectUri,
        int linkLifespanSeconds
    ) {
        realm().users().get(keycloakUserId.toString()).executeActionsEmail(
            clientId,
            redirectUri,
            linkLifespanSeconds,
            List.of("UPDATE_PASSWORD")
        );
    }

    private void updateEnabled(UUID keycloakUserId, boolean enabled) {
        UserResource user = realm().users().get(keycloakUserId.toString());
        UserRepresentation representation = user.toRepresentation();
        representation.setEnabled(enabled);
        user.update(representation);
    }

    private RealmResource realm() {
        return client().realm(keycloakConfig.getRealm());
    }

    private Keycloak client() {
        if (keycloak == null) {
            synchronized (this) {
                if (keycloak == null) {
                    keycloak = KeycloakBuilder.builder()
                        .serverUrl(keycloakConfig.getServerUrl())
                        .realm(keycloakConfig.getRealm())
                        .grantType(org.keycloak.OAuth2Constants.CLIENT_CREDENTIALS)
                        .clientId(keycloakConfig.getClientId())
                        .clientSecret(keycloakConfig.getClientSecret())
                        .build();
                }
            }
        }
        return keycloak;
    }
}
