package com.montreal.msiav_bh.utils;

import com.montreal.core.domain.exception.FileException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FileUtilsTest {

    @Test
    @DisplayName("Deve retornar o tipo MIME correto para um arquivo MultipartFile")
    void shouldReturnMimeTypeFromMultipartFile() {
        MultipartFile file = new MockMultipartFile("test.pdf", "Test PDF Content".getBytes());

        String mimeType = FileUtils.getMimeTypeFromMultipartFile(file);

        assertEquals("text/plain", mimeType);
    }

    @Test
    @DisplayName("Deve retornar null quando o arquivo MultipartFile é inválido")
    void shouldReturnNullWhenMultipartFileIsNull() {
        MultipartFile file = null;

        String mimeType = FileUtils.getMimeTypeFromMultipartFile(file);

        assertNull(mimeType);
    }

    @Test
    @DisplayName("Deve converter Resource para string Base64 quando o Resource é válido")
    void shouldConvertResourceToBase64WhenResourceIsValid() throws IOException {
        Resource resource = mock(Resource.class);
        byte[] testData = "Test Content".getBytes();
        InputStream inputStream = new ByteArrayInputStream(testData);
        when(resource.getInputStream()).thenReturn(inputStream);
        String expectedBase64 = Base64.getEncoder().encodeToString(testData);

        String result = FileUtils.resourceToBase64(resource);

        assertThat(result).isEqualTo(expectedBase64);
    }

    @Test
    @DisplayName("Deve lançar FileException quando ocorre erro ao ler o Resource")
    void shouldThrowFileExceptionWhenErrorOccursReadingResource() throws IOException {
        Resource resource = mock(Resource.class);
        when(resource.getInputStream()).thenThrow(new IOException("Error reading resource"));

        assertThatThrownBy(() -> FileUtils.resourceToBase64(resource))
                .isInstanceOf(FileException.class)
                .hasMessage("Erro ao converter arquivo base64")
                .hasCauseInstanceOf(IOException.class);
    }
}