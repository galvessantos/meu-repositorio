package com.montreal.msiav_bh.repository;

import com.montreal.msiav_bh.entity.Notary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotaryRepository extends JpaRepository<Notary, Long> {
    Optional<Notary> findByContratoId(Long contratoId);
}
