package com.montreal.msiav_bh.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "collected")
public class Collected {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_found", nullable = false)
    private Boolean vehicleFound;

    @Column(length = 500)
    private String note;

    @Column(name = "collection_date_time")
    private LocalDateTime collectionDateTime;
}
