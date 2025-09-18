package com.montreal.msiav_bh.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "file_report")
public class FileReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "A data de recebimento não pode estar vazia")
    @Column(name = "received_date", nullable = false)
    private String receivedDate; // Data de recebimento do arquivo

    @NotBlank(message = "O nome do arquivo não pode estar vazio")
    @Column(name = "file_name", nullable = false)
    private String fileName; // Nome do arquivo

    @NotBlank(message = "O tipo do arquivo não pode estar vazio")
    @Column(name = "file_type", nullable = false)
    private String fileType; // Tipo do arquivo (ex: PDF)

    @NotBlank(message = "O caminho do arquivo não pode estar vazio")
    @Column(name = "file_path", nullable = false)
    private String filePath; // Caminho onde o arquivo está armazenado

    @NotNull(message = "O relatório associado não pode ser nulo")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report; // Relacionamento com a entidade Report

    @NotNull(message = "Os dados do arquivo PDF não podem ser nulos")
    @Column(name = "pdf_data", nullable = false)
    private byte[] pdfData; // Dados binários do PDF armazenado

    @Column(name = "storage_id")
    private String storageId;
}
