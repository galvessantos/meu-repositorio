package com.montreal.msiav_bh.utils;

import com.montreal.msiav_bh.dto.FileDownloadDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class FileDownloadUtilsTest {

    private final String fileName = "test";
    private final byte[] fileContent = "Test PDF Content".getBytes();
    private final Long documentId = 123L;

    @Test
    @DisplayName("Should successfully retrieve file download DTO when valid content provided")
    void shouldReturnFileDownloadDTO() {
        FileDownloadDTO result = FileDownloadUtils.get(documentId, fileName, fileContent);

        assertThat(result).isNotNull();
        assertThat(result.fileName()).contains(fileName).endsWith(".pdf");
        assertThat(result.fileSize()).isEqualTo(fileContent.length);
        assertThat(result.file()).isNotNull();
    }

    @Test
    @DisplayName("Should create file with correct content")
    void shouldCreateFileWithCorrectContent() throws IOException {
        FileDownloadDTO result = FileDownloadUtils.get(documentId, fileName, fileContent);
        Resource resource = result.file();

        assertThat(resource).isNotNull();
        assertThat(resource.contentLength()).isEqualTo(fileContent.length);

        byte[] actualContent = new byte[(int)resource.contentLength()];
        resource.getInputStream().read(actualContent);
        assertThat(actualContent).isEqualTo(fileContent);
    }

    @Test
    @DisplayName("Should remove temporary file after creating resource")
    void shouldRemoveTemporaryFileAfterCreatingResource() {
        FileDownloadDTO result = FileDownloadUtils.get(documentId, fileName, fileContent);

        String filePath = result.fileName();
        Path path = Path.of(filePath);
        assertThat(Files.exists(path)).isFalse();
    }
}