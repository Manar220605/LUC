package com.luc.qa.module.user.service;

import com.luc.qa.module.alumni.repository.AlumniProfileRepository;
import com.luc.qa.module.user.dto.PublicAuthorDTO;
import com.luc.qa.module.user.entity.User;
import com.luc.qa.module.user.entity.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicAuthorService {

    private final AlumniProfileRepository alumniProfileRepository;

    public PublicAuthorDTO fromUser(User user) {
        PublicAuthorDTO.PublicAuthorDTOBuilder builder = PublicAuthorDTO.builder()
            .id(user.getId())
            .displayName(user.getDisplayName())
            .role(user.getRole())
            .avatarUrl(user.getAvatarUrl());

        if (user.getRole() == UserRole.ALUMNI) {
            alumniProfileRepository.findById(user.getId()).ifPresent(profile -> {
                if (profile.isPublicProfile()) {
                    builder.gradYear(profile.getGradYear())
                        .currentPosition(profile.getCurrentPosition());
                }
            });
        }

        return builder.build();
    }

    public PublicAuthorDTO anonymous() {
        return PublicAuthorDTO.builder()
            .displayName("Anonymous")
            .build();
    }

    public PublicAuthorDTO deleted() {
        return PublicAuthorDTO.builder()
            .displayName("[deleted]")
            .build();
    }
}
