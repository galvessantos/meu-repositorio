package com.montreal.msiav_bh.repository;

import com.montreal.msiav_bh.entity.Creditor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CreditorRepository extends JpaRepository<Creditor, Long> {
    Optional<Creditor> findByContratoId(Long contratoId);
}
