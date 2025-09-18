package com.montreal.msiav_bh.repository;

import com.montreal.msiav_bh.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContractRepository extends JpaRepository<Contract, Long> {

    Optional<Contract> findByNumero(String numero);
}
