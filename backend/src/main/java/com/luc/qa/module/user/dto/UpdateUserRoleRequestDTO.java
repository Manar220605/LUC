package com.luc.qa.module.user.dto;

import com.luc.qa.module.user.entity.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRoleRequestDTO {

    @NotNull
    private UserRole role;
}
