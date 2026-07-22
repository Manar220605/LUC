package com.luc.qa.module.user.dto;

import com.luc.qa.module.user.entity.UserRole;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminUserResponseDTO {

    private Long id;
    private String email;
    private String displayName;
    private UserRole role;
    private String studentId;
    private boolean banned;
    private String banReason;
    private Instant createdAt;
}
