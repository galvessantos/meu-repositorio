package com.montreal.msiav_bh.repository;

import com.montreal.msiav_bh.entity.Detran;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DetranRepository extends JpaRepository<Detran, Long> {
    Optional<Detran> findByContratoId(Long contratoId);
}
