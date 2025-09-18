package com.montreal.msiav_bh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.montreal.msiav_bh.entity.SeizureDate;

@Repository
public interface SeizureDateRepository extends JpaRepository<SeizureDate, Long> {

    @Query("SELECT s FROM SeizureDate s WHERE s.vehicle.id = :vehicleId")
    List<SeizureDate> findByVehicleId(@Param("vehicleId") Long vehicleId);
}
