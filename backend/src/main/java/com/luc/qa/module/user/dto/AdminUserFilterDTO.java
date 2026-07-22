package com.luc.qa.module.user.dto;

import com.luc.qa.module.user.entity.UserRole;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserFilterDTO {

    private UserRole role;
    private Boolean isBanned;
    private String search;
    private int page = 0;
    private int size = 20;
}
