package com.montreal.core.domain.service;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.montreal.core.domain.dto.FileDataDTO;
import com.montreal.core.domain.exception.FileException;
import com.montreal.core.domain.properties.FileProperties;
import com.montreal.msiav_bh.enumerations.VisionTypeEnum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileSystemService {

    private final FileProperties fileProperties;

    public FileDataDTO processAndSaveFile(MultipartFile file, Long vehicleId, VisionTypeEnum visionType) {
        log.info("Processando e salvando arquivo do veiculo {} - type: {}", vehicleId, visionType);

        try {
            long fileSize = file.getSize();
            long maxSize = fileProperties.getMaxUploadSizeInBytes();

            if (fileSize > maxSize) {
                String maxSizeMB = String.format("%.2f", maxSize / (1024.0 * 1024.0));
                String fileSizeMB = String.format("%.2f", fileSize / (1024.0 * 1024.0));
                throw new FileException(String.format(
                    "A imagem enviada possui %.2fMB e excede o tamanho máximo permitido de %sMB. Por favor, envie uma imagem menor.",
                    Double.parseDouble(fileSizeMB), maxSizeMB
                ));
            }

            String name = String.format("%s_%s", visionType, file.getOriginalFilename());
            var uploadDir = fileProperties.getUploadDir();
            var filePath = Paths.get(uploadDir, String.valueOf(vehicleId), name);

            Files.createDirectories(filePath.getParent());
            Files.write(filePath, file.getBytes());

            log.info("Arquivo {} do veiculo {} - type: {} salvo com sucesso", name, vehicleId, visionType);

            return FileDataDTO.builder()
                    .name(name)
                    .originalSize(fileSize + " bytes")
                    .imageType(file.getContentType())
                    .imageUrl(filePath.toString())
                    .build();

        } catch (FileException e) {
            throw e;
        } catch (Exception e) {
            throw new FileException(String.format("Erro ao salvar o arquivo do veículo %s - Erro: %s",
                    vehicleId, e.getMessage()), e);
        }
    }


}
