package com.montreal.msiav_bh.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.montreal.msiav_bh.entity.History;

@Repository
public interface HistoryRepository extends JpaRepository<History, Long> {

    @Query("SELECT h FROM History h WHERE h.vehicle.id = :vehicleId")
    List<History> findByIdVehicle(@Param("vehicleId") Long vehicleId);
    
    @Query("SELECT h FROM History h WHERE h.vehicle.id = :vehicleId")
    Page<History> findByVehicleId(@Param("vehicleId") Long vehicleId, Pageable pageable);

}