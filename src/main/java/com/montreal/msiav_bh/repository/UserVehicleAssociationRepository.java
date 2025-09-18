package com.montreal.msiav_bh.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.montreal.msiav_bh.entity.UserVehicleAssociation;

@Repository
public interface UserVehicleAssociationRepository extends JpaRepository<UserVehicleAssociation, Long> {

    @Query("SELECT u FROM UserVehicleAssociation u WHERE u.user.id = :userId AND u.vehicle.id = :vehicleId")
    Optional<UserVehicleAssociation> findByUserIdAndVehicleId(@Param("userId") Long userId, @Param("vehicleId") Long vehicleId);

    @Query("SELECT u FROM UserVehicleAssociation u WHERE u.user.id = :userId")
    List<UserVehicleAssociation> findAllByUserId(@Param("userId") Long userId);

    @Query("SELECT u FROM UserVehicleAssociation u WHERE u.vehicle.id = :vehicleId")
    List<UserVehicleAssociation> findAllByVehicleId(@Param("vehicleId") Long vehicleId);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN TRUE ELSE FALSE END FROM UserVehicleAssociation u WHERE u.user.id = :userId AND u.vehicle.id = :vehicleId")
    boolean existsByUserIdAndVehicleId(@Param("userId") Long userId, @Param("vehicleId") Long vehicleId);

    @Query("SELECT u FROM UserVehicleAssociation u WHERE u.user.id = :userId")
    List<UserVehicleAssociation> findByUserId(@Param("userId") Long userId);

    @Query("SELECT u FROM UserVehicleAssociation u WHERE u.vehicle.id = :vehicleId")
    Optional<UserVehicleAssociation> findByVehicleId(@Param("vehicleId") Long vehicleId);
    
}
