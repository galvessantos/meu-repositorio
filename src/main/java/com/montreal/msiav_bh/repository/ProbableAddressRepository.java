package com.montreal.msiav_bh.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.montreal.msiav_bh.entity.ProbableAddress;

@Repository
public interface ProbableAddressRepository extends JpaRepository<ProbableAddress, Long> {
	
    boolean existsByVehicleId(Long vehicleId);
    
    Optional<ProbableAddress> findByVehicleIdAndAddressId(Long vehicleId, Long addressId);

    List<ProbableAddress> findAllByVehicleId(Long vehicleId);
}