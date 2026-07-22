package com.luc.qa.module.user.mapper;

import com.luc.qa.module.user.dto.AdminUserResponseDTO;
import com.luc.qa.module.user.dto.UserResponseDTO;
import com.luc.qa.module.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponseDTO toResponse(User user);

    AdminUserResponseDTO toAdminResponse(User user);
}
