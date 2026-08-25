package com.luc.qa.module.user.service;

import com.luc.qa.module.user.dto.PublicProfileDTO;

public interface PublicProfileService {

    PublicProfileDTO getPublicProfile(Long userId);
}
