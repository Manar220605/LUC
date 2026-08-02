package com.luc.qa.module.user.dto;

import com.luc.qa.module.user.entity.UserRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PublicAuthorDTO {

    private Long id;
    private String displayName;
    private UserRole role;
    private String avatarUrl;
}
