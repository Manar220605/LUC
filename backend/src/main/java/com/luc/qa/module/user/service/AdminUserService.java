package com.luc.qa.module.user.service;

import com.luc.qa.common.pagination.PageResponseDTO;
import com.luc.qa.module.user.dto.AdminUserFilterDTO;
import com.luc.qa.module.user.entity.User;
import com.luc.qa.module.user.entity.UserRole;

public interface AdminUserService {

    PageResponseDTO<User> listUsers(AdminUserFilterDTO filter);

    User banUser(Long id, String reason);

    User unbanUser(Long id);

    User updateRole(Long id, UserRole role);
}
