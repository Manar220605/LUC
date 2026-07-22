package com.luc.qa.module.user.dto;

import com.luc.qa.module.user.entity.UserRole;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponseDTO {

    private Long id;
    private String email;
    private String displayName;
    private String avatarUrl;
    private String bio;
    private UserRole role;
    private String studentId;
    private Instant verifiedStudentAt;
    private Instant verifiedAlumniAt;
    private Instant createdAt;
}
