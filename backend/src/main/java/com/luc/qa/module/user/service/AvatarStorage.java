package com.luc.qa.module.user.service;

import com.luc.qa.common.exception.BadRequestException;
import com.luc.qa.common.exception.NotFoundException;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class AvatarStorage {

    static final String AVATAR_URL_PREFIX = "/api/uploads/avatars/";
    private static final Pattern STORED_AVATAR = Pattern.compile(
        "^/api/uploads/avatars/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\.(jpg|png|webp)$"
    );
    private static final Pattern SAFE_FILENAME = Pattern.compile(
        "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\.(jpg|png|webp)$"
    );

    private final Path avatarsDir;
    private final long maxAvatarBytes;

    public AvatarStorage(
        @Value("${luc.uploads.dir:uploads}") String uploadsDir,
        @Value("${luc.uploads.max-avatar-bytes:2097152}") long maxAvatarBytes
    ) {
        this.avatarsDir = Path.of(uploadsDir, "avatars").toAbsolutePath().normalize();
        this.maxAvatarBytes = maxAvatarBytes;
    }

    @PostConstruct
    void createDirectories() throws IOException {
        Files.createDirectories(avatarsDir);
    }

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Choose a photo to upload");
        }
        if (file.getSize() > maxAvatarBytes) {
            throw new BadRequestException("Photo is too large. Maximum size is 2 MB.");
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException ex) {
            throw new BadRequestException("Could not read the photo");
        }
        if (bytes.length > maxAvatarBytes) {
            throw new BadRequestException("Photo is too large. Maximum size is 2 MB.");
        }

        ImageType type = detect(bytes);
        String filename = UUID.randomUUID() + "." + type.extension;
        Path destination = avatarsDir.resolve(filename).normalize();
        if (!destination.startsWith(avatarsDir)) {
            throw new BadRequestException("Could not store the photo");
        }
        try {
            Files.write(destination, bytes);
        } catch (IOException ex) {
            log.error("Failed to write avatar {}", destination, ex);
            throw new BadRequestException("Could not store the photo");
        }
        return AVATAR_URL_PREFIX + filename;
    }

    public void deleteIfStored(String avatarUrl) {
        if (avatarUrl == null || !STORED_AVATAR.matcher(avatarUrl).matches()) {
            return;
        }
        String filename = avatarUrl.substring(AVATAR_URL_PREFIX.length());
        Path file = avatarsDir.resolve(filename).normalize();
        if (!file.startsWith(avatarsDir)) {
            return;
        }
        try {
            Files.deleteIfExists(file);
        } catch (IOException ex) {
            log.warn("Could not delete old avatar {}", file, ex);
        }
    }

    public StoredAvatar load(String filename) {
        if (filename == null || !SAFE_FILENAME.matcher(filename).matches()) {
            throw new AvatarNotFoundException(filename);
        }
        Path file = avatarsDir.resolve(filename).normalize();
        if (!file.startsWith(avatarsDir) || !Files.isRegularFile(file)) {
            throw new AvatarNotFoundException(filename);
        }
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        return new StoredAvatar(new FileSystemResource(file.toFile()), mediaType(extension));
    }

    private static ImageType detect(byte[] bytes) {
        if (bytes.length >= 3 && bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xD8 && bytes[2] == (byte) 0xFF) {
            return ImageType.JPEG;
        }
        if (bytes.length >= 8
            && bytes[0] == (byte) 0x89
            && bytes[1] == 0x50
            && bytes[2] == 0x4E
            && bytes[3] == 0x47
            && bytes[4] == 0x0D
            && bytes[5] == 0x0A
            && bytes[6] == 0x1A
            && bytes[7] == 0x0A) {
            return ImageType.PNG;
        }
        if (bytes.length >= 12
            && bytes[0] == 'R'
            && bytes[1] == 'I'
            && bytes[2] == 'F'
            && bytes[3] == 'F'
            && bytes[8] == 'W'
            && bytes[9] == 'E'
            && bytes[10] == 'B'
            && bytes[11] == 'P') {
            return ImageType.WEBP;
        }
        throw new BadRequestException("Use a JPEG, PNG, or WebP photo");
    }

    private static MediaType mediaType(String extension) {
        return switch (extension) {
            case "png" -> MediaType.IMAGE_PNG;
            case "webp" -> MediaType.parseMediaType("image/webp");
            default -> MediaType.IMAGE_JPEG;
        };
    }

    public record StoredAvatar(Resource resource, MediaType mediaType) {}

    private enum ImageType {
        JPEG("jpg"),
        PNG("png"),
        WEBP("webp");

        private final String extension;

        ImageType(String extension) {
            this.extension = extension;
        }
    }

    static final class AvatarNotFoundException extends NotFoundException {
        AvatarNotFoundException(String filename) {
            super("Avatar not found: " + filename);
        }
    }
}
