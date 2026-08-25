package com.luc.qa.module.user.service;

import com.luc.qa.module.user.dto.OnboardingRequestDTO;
import com.luc.qa.module.user.dto.UpdateProfileRequestDTO;
import com.luc.qa.module.user.entity.User;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    User getByKeycloakId(String keycloakId);

    User updateProfile(String keycloakId, UpdateProfileRequestDTO request);

    User updateAvatar(String keycloakId, MultipartFile file);

    User clearAvatar(String keycloakId);

    User completeOnboarding(String keycloakId, OnboardingRequestDTO request);
}
