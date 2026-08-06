package com.luc.qa.module.alumni.service;

import com.luc.qa.common.exception.AlumniProfileNotFoundException;
import com.luc.qa.common.exception.UserNotFoundException;
import com.luc.qa.module.alumni.dto.AlumniProfileResponseDTO;
import com.luc.qa.module.alumni.dto.UpdateAlumniProfileRequestDTO;
import com.luc.qa.module.alumni.entity.AlumniProfile;
import com.luc.qa.module.alumni.mapper.AlumniProfileMapper;
import com.luc.qa.module.alumni.repository.AlumniProfileRepository;
import com.luc.qa.module.user.entity.UserRole;
import com.luc.qa.module.user.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AlumniProfileServiceImpl implements AlumniProfileService {

    private final AlumniProfileRepository alumniProfileRepository;
    private final UserRepository userRepository;
    private final AlumniProfileMapper alumniProfileMapper;

    @Override
    @Transactional(readOnly = true)
    public AlumniProfileResponseDTO getMine(String keycloakId) {
        var user = userRepository.findByKeycloakId(UUID.fromString(keycloakId))
            .orElseThrow(() -> new UserNotFoundException("Keycloak sub " + keycloakId));
        if (user.getRole() != UserRole.ALUMNI) {
            throw new AlumniProfileNotFoundException();
        }
        AlumniProfile profile = alumniProfileRepository.findById(user.getId())
            .orElseThrow(AlumniProfileNotFoundException::new);
        return alumniProfileMapper.toResponse(profile);
    }

    @Override
    public AlumniProfileResponseDTO updateMine(String keycloakId, UpdateAlumniProfileRequestDTO request) {
        var user = userRepository.findByKeycloakId(UUID.fromString(keycloakId))
            .orElseThrow(() -> new UserNotFoundException("Keycloak sub " + keycloakId));
        if (user.getRole() != UserRole.ALUMNI) {
            throw new AlumniProfileNotFoundException();
        }
        AlumniProfile profile = alumniProfileRepository.findById(user.getId())
            .orElseThrow(AlumniProfileNotFoundException::new);

        if (request.getCurrentPosition() != null) {
            profile.setCurrentPosition(trimToNull(request.getCurrentPosition()));
        }
        if (request.getCurrentCompany() != null) {
            profile.setCurrentCompany(trimToNull(request.getCurrentCompany()));
        }
        if (request.getIsPublic() != null) {
            profile.setPublicProfile(request.getIsPublic());
        }

        return alumniProfileMapper.toResponse(alumniProfileRepository.save(profile));
    }

    private String trimToNull(String value) {
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
