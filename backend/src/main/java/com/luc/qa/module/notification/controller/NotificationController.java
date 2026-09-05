package com.luc.qa.module.notification.controller;

import com.luc.qa.common.pagination.PageResponseDTO;
import com.luc.qa.module.notification.dto.NotificationResponseDTO;
import com.luc.qa.module.notification.dto.UnreadCountResponseDTO;
import com.luc.qa.module.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(
        summary = "List my notifications",
        description = "Returns a paginated list of notifications for the authenticated user. Requires JWT authentication."
    )
    public PageResponseDTO<NotificationResponseDTO> list(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "false") boolean unreadOnly,
        @AuthenticationPrincipal Jwt jwt
    ) {
        return notificationService.listForCurrentUser(jwt.getSubject(), page, size, unreadOnly);
    }

    @GetMapping("/unread-count")
    @Operation(
        summary = "Get unread notification count",
        description = "Returns the unread notification count for the authenticated user. Requires JWT authentication."
    )
    public UnreadCountResponseDTO unreadCount(@AuthenticationPrincipal Jwt jwt) {
        return notificationService.getUnreadCount(jwt.getSubject());
    }

    @PostMapping("/{id}/read")
    @Operation(
        summary = "Mark notification as read",
        description = "Marks a single notification as read for the authenticated user. Requires JWT authentication."
    )
    public NotificationResponseDTO markAsRead(
        @PathVariable Long id,
        @AuthenticationPrincipal Jwt jwt
    ) {
        return notificationService.markAsRead(id, jwt.getSubject());
    }

    @PostMapping("/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
        summary = "Mark all notifications as read",
        description = "Marks all notifications as read for the authenticated user. Requires JWT authentication."
    )
    public void markAllAsRead(@AuthenticationPrincipal Jwt jwt) {
        notificationService.markAllAsRead(jwt.getSubject());
    }
}
