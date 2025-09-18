package com.montreal.msiav_bh.entity;

import java.time.LocalDateTime;

import com.montreal.oauth.domain.entity.UserInfo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(name = "user_vehicle_association", 
       uniqueConstraints = @UniqueConstraint(name = "unique_user_vehicle", columnNames = {"user_id", "vehicle_id"}))
public class UserVehicleAssociation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "O usuário é obrigatório.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_vehicle_association_user"))
    private UserInfo user;

    @NotNull(message = "O veículo é obrigatório.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_vehicle_association_vehicle"))
    private Vehicle vehicle;

    @NotNull(message = "O usuário responsável pelo vínculo é obrigatório.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "associated_by", nullable = false, foreignKey = @ForeignKey(name = "fk_user_vehicle_association_associated_by"))
    private UserInfo associatedBy;

    @NotNull(message = "A data de criação é obrigatória.")
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

}
