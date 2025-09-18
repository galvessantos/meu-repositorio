package com.montreal.msiav_bh.utils;

import com.montreal.core.domain.exception.InternalErrorException;
import com.montreal.core.domain.exception.NotFoundException;
import com.montreal.msiav_bh.dto.FileDownloadDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FileDownloadUtils {

    public static FileDownloadDTO get(Object id, String fileName, byte[] fileContent) {
        try {
            File file = File.createTempFile(fileName, ".pdf");
            writeFile(file, fileContent);

            if (!file.exists()) {
                throw new NotFoundException("Arquivo PDF do relatório com ID " + id + " não encontrado.");
            }

            return getFileDownloadDTO(file)
                    .orElseThrow(() -> new NotFoundException("Arquivo PDF do relatório com ID " + id + " não encontrado."));
        } catch (Exception e) {
            log.error("Erro ao tentar criar arquivo temporário {}", e.getMessage(), e);
            throw new InternalErrorException(e.getMessage(), e);
        }
    }

    private static void writeFile(File file, byte[] fileContent) {
        try (var fileOutputStream = new FileOutputStream(file)) {
            fileOutputStream.write(fileContent);
        } catch (IOException e) {
            log.error("Erro ao tentar salvar arquivo {}", e.getMessage(), e);
        }
    }

    public static Optional<FileDownloadDTO> getFileDownloadDTO(File file) {
        try {
            return Optional.of(FileDownloadDTO
                    .builder()
                    .fileName(file.getName())
                    .fileSize(file.length())
                    .file(getFileResource(file))
                    .build());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return Optional.empty();
        }
    }

    private static Resource getFileResource(File file) throws IOException {
        Path path = Paths.get(file.getAbsolutePath());
        Resource resource = new ByteArrayResource(Files.readAllBytes(path));
        Files.deleteIfExists(path);

        return resource;
    }
}
