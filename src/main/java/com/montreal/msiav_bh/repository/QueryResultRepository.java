package com.montreal.msiav_bh.repository;

import com.montreal.msiav_bh.entity.QueryResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface QueryResultRepository extends JpaRepository<QueryResult, Long> {
    Optional<QueryResult> findByVehicleId(Long vehicleId);
}
