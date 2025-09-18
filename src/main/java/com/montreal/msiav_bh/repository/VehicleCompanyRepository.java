package com.montreal.msiav_bh.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.montreal.msiav_bh.entity.VehicleCompany;

@Repository
public interface VehicleCompanyRepository extends JpaRepository<VehicleCompany, Long> {

    @Query("SELECT vc FROM VehicleCompany vc JOIN FETCH vc.company WHERE vc.vehicle.id = :vehicleId AND vc.company.id = :companyId")
    Optional<VehicleCompany> findByVehicleIdAndCompanyId(@Param("vehicleId") Long vehicleId, @Param("companyId") Long companyId);

    @Query("SELECT vc FROM VehicleCompany vc JOIN FETCH vc.company WHERE vc.vehicle.id = :vehicleId")
    List<VehicleCompany> findByVehicleId(@Param("vehicleId") Long vehicleId);

    @Query("SELECT vc FROM VehicleCompany vc JOIN FETCH vc.company WHERE vc.vehicle.id = :vehicleId AND vc.company.id = :companyId")
    List<VehicleCompany> findAllByVehicleIdAndCompanyId(@Param("vehicleId") Long vehicleId, @Param("companyId") Long companyId);

    @Query("SELECT vc FROM VehicleCompany vc JOIN FETCH vc.company WHERE vc.vehicle.id = :vehicleId")
    List<VehicleCompany> findVehicleCompaniesByVehicleId(@Param("vehicleId") Long vehicleId);

    @Query(value = """ 
        select vc.*
        from vehicle_company vc
        inner join vehicle_seizure vs on vc.company_id = vs.company_id and vc.vehicle_id = vs.vehicle_id
        where vs.id = :vehicleSeizureId
""", nativeQuery = true)
    Optional<VehicleCompany> findByVehicleSeizureId(Long vehicleSeizureId);
}
