package com.montreal.msiav_bh.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.montreal.msiav_bh.entity.VehicleImages;

@Repository
public interface VehicleImagesRepository extends JpaRepository<VehicleImages, Long> {

	@Query("SELECT vi FROM VehicleImages vi WHERE vi.vehicle.id = :vehicleId")
	Page<VehicleImages> findByVehicleId(@Param("vehicleId") Long vehicleId, Pageable pageable);

	@Query("SELECT vi FROM VehicleImages vi WHERE vi.vehicle.id = :vehicleId")
	List<VehicleImages> findByVehicleId(@Param("vehicleId") Long vehicleId);

	@Query("SELECT vi FROM VehicleImages vi WHERE vi.vehicleSeizure.id = :vehicleSeizureId")
	Page<VehicleImages> findByVehicleSeizureId(@Param("vehicleSeizureId") Long vehicleSeizureId, Pageable pageable);

	@Query("SELECT vi FROM VehicleImages vi WHERE vi.vehicleSeizure.id = :vehicleSeizureId")
	List<VehicleImages> findByVehicleSeizureId(@Param("vehicleSeizureId") Long vehicleSeizureId);
}

