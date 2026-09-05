package com.luc.qa.module.user.controller;

import com.luc.qa.common.pagination.PageResponseDTO;
import com.luc.qa.module.user.dto.AdminUserFilterDTO;
import com.luc.qa.module.user.dto.AdminUserResponseDTO;
import com.luc.qa.module.user.dto.BanUserRequestDTO;
import com.luc.qa.module.user.dto.UpdateUserRoleRequestDTO;
import com.luc.qa.module.user.mapper.UserMapper;
import com.luc.qa.module.user.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Users")
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final UserMapper userMapper;

    @GetMapping
    @Operation(
        summary = "List users (admin)",
        description = "Returns a paginated, filterable list of users. Requires ADMIN role."
    )
    public PageResponseDTO<AdminUserResponseDTO> list(@ModelAttribute AdminUserFilterDTO filter) {
        var page = adminUserService.listUsers(filter);
        List<AdminUserResponseDTO> content = page.getContent().stream()
            .map(userMapper::toAdminResponse)
            .toList();
        return PageResponseDTO.<AdminUserResponseDTO>builder()
            .content(content)
            .page(page.getPage())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .build();
    }

    @PutMapping("/{id}/ban")
    @Operation(
        summary = "Ban user (admin)",
        description = "Bans a user with the given reason. Requires ADMIN role."
    )
    public AdminUserResponseDTO ban(
        @PathVariable Long id,
        @Valid @RequestBody BanUserRequestDTO request
    ) {
        return userMapper.toAdminResponse(adminUserService.banUser(id, request.getReason()));
    }

    @PutMapping("/{id}/unban")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
        summary = "Unban user (admin)",
        description = "Removes a ban from a user. Requires ADMIN role."
    )
    public void unban(@PathVariable Long id) {
        adminUserService.unbanUser(id);
    }

    @PutMapping("/{id}/role")
    @Operation(
        summary = "Update user role (admin)",
        description = "Changes a user's role. Requires ADMIN role."
    )
    public AdminUserResponseDTO updateRole(
        @PathVariable Long id,
        @Valid @RequestBody UpdateUserRoleRequestDTO request
    ) {
        return userMapper.toAdminResponse(adminUserService.updateRole(id, request.getRole()));
    }
}
