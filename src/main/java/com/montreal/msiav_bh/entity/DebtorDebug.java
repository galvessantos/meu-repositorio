package com.montreal.msiav_bh.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.montreal.core.utils.CryptoUtil;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "debtor_debug")
public class DebtorDebug {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "address_id", referencedColumnName = "id", nullable = false)
    private Address address;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "cpf_cnpj", nullable = false, unique = true)
    private String cpfCnpj;

    @Column(name = "email")
    private String email;

    @Column(name = "cell_phone")
    private String cellPhone;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "contrato_id")
    @JsonBackReference
    private Contract contrato;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public void encryptFields(CryptoUtil crypto) {
        if (name != null) name = crypto.encrypt(name);
        if (cpfCnpj != null) cpfCnpj = crypto.encrypt(cpfCnpj);
        if (email != null) email = crypto.encrypt(email);
        if (cellPhone != null) cellPhone = crypto.encrypt(cellPhone);
    }

    public void decryptFields(CryptoUtil crypto) {
        if (name != null) name = crypto.decrypt(name);
        if (cpfCnpj != null) cpfCnpj = crypto.decrypt(cpfCnpj);
        if (email != null) email = crypto.decrypt(email);
        if (cellPhone != null) cellPhone = crypto.decrypt(cellPhone);
    }
}
