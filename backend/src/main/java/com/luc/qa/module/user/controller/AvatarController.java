package com.luc.qa.module.user.controller;

import com.luc.qa.module.user.service.AvatarStorage;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/uploads/avatars")
@RequiredArgsConstructor
public class AvatarController {

    private final AvatarStorage avatarStorage;

    @GetMapping("/{filename}")
    public ResponseEntity<Resource> get(@PathVariable String filename) {
        AvatarStorage.StoredAvatar avatar = avatarStorage.load(filename);
        return ResponseEntity.ok()
            .contentType(avatar.mediaType())
            .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic())
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
            .body(avatar.resource());
    }
}
