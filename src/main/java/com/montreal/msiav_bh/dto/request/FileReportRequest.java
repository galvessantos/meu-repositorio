package com.montreal.msiav_bh.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileReportRequest {

    @NotBlank(message = "A data de recebimento não pode estar vazia")
    private String receivedDate;

    @NotBlank(message = "O nome do arquivo não pode estar vazio")
    private String fileName;

    @NotBlank(message = "O tipo do arquivo não pode estar vazio")
    private String fileType;

    @NotBlank(message = "O caminho do arquivo não pode estar vazio")
    private String filePath;

    @NotNull(message = "O relatório associado não pode ser nulo")
    private Long reportId;

    @NotNull(message = "Os dados do arquivo PDF não podem ser nulos")
    private byte[] pdfData;
}
