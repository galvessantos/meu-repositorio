package com.montreal.msiav_bh.enumerations;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum ImageMimeTypeEnum {
    JPEG("image/jpeg", "jpeg"),
    JPG("image/jpeg", "jpg"),
    PNG("image/png", "png"),
    HEIC("image/heic", "heic"),;

    private final String type;
    private final String extension;

    public static Optional<ImageMimeTypeEnum> fromType(String type) {
        for (ImageMimeTypeEnum imageMimeTypeEnum : ImageMimeTypeEnum.values()) {
            if (imageMimeTypeEnum.getType().equals(type)) {
                return Optional.of(imageMimeTypeEnum);
            }
        }
        return Optional.empty();
    }
}
