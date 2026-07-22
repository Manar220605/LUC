package com.luc.qa.module.user.service;

import com.luc.qa.module.user.dto.OnboardingRequestDTO;
import com.luc.qa.module.user.dto.UpdateProfileRequestDTO;
import com.luc.qa.module.user.entity.User;

public interface UserService {

    User getByKeycloakId(String keycloakId);

    User updateProfile(String keycloakId, UpdateProfileRequestDTO request);

    User completeOnboarding(String keycloakId, OnboardingRequestDTO request);
}
