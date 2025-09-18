package com.montreal.msiav_bh.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.montreal.msiav_bh.enumerations.VisionTypeEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "vehicle_images")
public class VehicleImages {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "O veículo associado é obrigatório.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle; // Veículo associado à imagem

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_seizure_id")
    private VehicleSeizure vehicleSeizure; // Apreensão do veículo (opcional)

    @NotNull(message = "O nome da imagem é obrigatório.")
    @Column(nullable = false)
    private String name; // Nome da imagem

    @Column(name = "original_size")
    private String originalSize; // Tamanho original da imagem

    @Column(name = "current_size")
    private String currentSize; // Tamanho atual da imagem após compressão/redimensionamento

    @Column(name = "image_type")
    private String imageType; // Tipo da imagem (ex: JPEG, PNG)

    @Column(name = "image_url")
    private String imageUrl; // URL da imagem armazenada

    @Enumerated(EnumType.STRING)
    @Column(name = "vision_type")
    private VisionTypeEnum visionType; // Tipo de visão (ex: FRONTAL, LATERAL)

    @UpdateTimestamp
    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt; // Data da última atualização

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // Data de criação do registro
}
