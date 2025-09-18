package com.montreal.msiav_bh.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String communicationMethod;
    private LocalDate sentDate;
    private LocalDate readDate;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(name = "evidence_file", columnDefinition = "TEXT")
    private String evidenceFileBase64;

    @ManyToOne
    @JoinColumn(name = "contrato_id")
    @JsonBackReference
    private Contract contrato;
}

