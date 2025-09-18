package com.montreal.msiav_bh.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.montreal.msiav_bh.entity.VehicleAddress;

@Repository
public interface VehicleAddressRepository extends JpaRepository<VehicleAddress, Long> {

    @Query("SELECT v FROM VehicleAddress v WHERE v.vehicle.id = :vehicleId")
    List<VehicleAddress> findByVehicleId(@Param("vehicleId") Long vehicleId);

    @Query("SELECT v FROM VehicleAddress v WHERE v.address.id = :addressId AND v.vehicle.id = :vehicleId")
    Optional<VehicleAddress> findByAddressIdAndVehicleId(@Param("addressId") Long addressId, @Param("vehicleId") Long vehicleId);

    @Query("SELECT v FROM VehicleAddress v WHERE v.address.id = :addressId")
    Optional<VehicleAddress> findByAddressId(@Param("addressId") Long addressId);

    @Query("SELECT v FROM VehicleAddress v WHERE v.vehicle.id = :vehicleId AND v.vehicle.id = :addressId")
    Optional<VehicleAddress> findByVehicleIdAndAddressId(@Param("vehicleId") Long vehicleId, @Param("addressId") Long addressId);
}