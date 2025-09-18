package com.montreal.oauth.controller;

import com.montreal.core.domain.exception.ImageNotAllowedException;
import com.montreal.oauth.domain.entity.UserImage;
import com.montreal.oauth.domain.service.UserImageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.montreal.msiav_bh.enumerations.ImageMimeTypeEnum.JPEG;
import static com.montreal.msiav_bh.enumerations.ImageMimeTypeEnum.PNG;
import static com.montreal.msiav_bh.utils.FileUtils.getMimeTypeFromMultipartFile;

@RestController
@RequestMapping("/api/v1/user-images")
@RequiredArgsConstructor
@Tag(name = "Imagem do Usuário")
public class UserImageController {

    public static final String MSG_ERROR_ALLOWED_FILE = "Apenas arquivos de imagem PNG, JPG e JPEG são permitidos.";
    public static final String MSG_SUCCESS_UPLOAD_FILE = "Imagem do usuário enviada com sucesso.";

    private static final Set<String> ALLOWED_FILE_TYPES = Set.of(PNG.getType(), JPEG.getType());

    private final UserImageService userImageService;

    @PostMapping(value = "/upload/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadUserImage(@PathVariable Long userId, @RequestParam("file") MultipartFile file) {
        String fileType = getMimeTypeFromMultipartFile(file);
        if (StringUtils.isNotBlank(fileType) && !ALLOWED_FILE_TYPES.contains(fileType)) {
            throw new ImageNotAllowedException(MSG_ERROR_ALLOWED_FILE);
        }

        userImageService.uploadUserImage(userId, file);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of("success", "true", "message", MSG_SUCCESS_UPLOAD_FILE));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<byte[]> getUserImage(@PathVariable Long userId) {
        Optional<UserImage> userImage = userImageService.getUserImage(userId);

        return userImage.map(image -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + image.getFileName() + "\"")
                        .contentType(MediaType.parseMediaType(image.getFileType()))
                        .body(image.getImageData()))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));

    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUserImage(@PathVariable Long userId) {
        userImageService.deleteUserImage(userId);
        return ResponseEntity.ok("Imagem do usuário removida com sucesso.");
    }
}
