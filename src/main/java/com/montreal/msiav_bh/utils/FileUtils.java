package com.montreal.msiav_bh.utils;

import com.montreal.core.domain.exception.FileException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FileUtils {

    public static String getMimeTypeFromMultipartFile(MultipartFile file) {
        try {
            return new Tika().detect(file.getInputStream());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public static String resourceToBase64(Resource resource) {
        try (InputStream is = resource.getInputStream()) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[4096];
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            return Base64.getEncoder().encodeToString(buffer.toByteArray());
        } catch (IOException e) {
            throw new FileException("Erro ao converter arquivo base64", e);
        }
    }
}
