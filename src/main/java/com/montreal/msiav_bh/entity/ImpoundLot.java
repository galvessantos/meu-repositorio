package com.montreal.msiav_bh.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "impound_lot")
public class ImpoundLot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "impound_arrival_date_time")
    private LocalDateTime impoundArrivalDateTime;

    @Column(name = "impound_departure_date_time")
    private LocalDateTime impoundDepartureDateTime;
}
