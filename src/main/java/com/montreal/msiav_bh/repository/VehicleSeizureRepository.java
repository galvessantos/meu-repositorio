package com.montreal.msiav_bh.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.montreal.msiav_bh.dto.SeizureNoticeDTO;
import com.montreal.msiav_bh.entity.VehicleSeizure;
import com.montreal.msiav_bh.enumerations.SeizureStatusEnum;
import com.montreal.msiav_bh.enumerations.VehicleStageEnum;

@Repository
public interface VehicleSeizureRepository extends JpaRepository<VehicleSeizure, Long> {

	@Query("""
	    SELECT vs 
	    FROM VehicleSeizure vs
	    JOIN FETCH vs.vehicle
	    JOIN FETCH vs.company
	    JOIN FETCH vs.address
	    JOIN FETCH vs.user
	    WHERE vs.vehicle.id = :vehicleId
	""")
	Optional<VehicleSeizure> findFirstByVehicleId(@Param("vehicleId") Long vehicleId);

    @Query("SELECT vs FROM VehicleSeizure vs WHERE vs.vehicle.id = :vehicleId AND vs.company.id = :companyId ORDER BY vs.id ASC")
    Optional<VehicleSeizure> findFirstByVehicleIdAndCompanyId(@Param("vehicleId") Long vehicleId, @Param("companyId") Long companyId);

    @Query("SELECT COUNT(vs) > 0 FROM VehicleSeizure vs WHERE vs.vehicle.id = :vehicleId")
    boolean existsByVehicleId(@Param("vehicleId") Long vehicleId);

    @Query("SELECT new com.montreal.msiav_bh.dto.SeizureNoticeDTO(" +
            "v.contractNumber, vs.vehicleCondition, " +
            "vs.seizureDate, vs.id, v.id) " +
            "FROM VehicleSeizure vs " +
            "JOIN vs.vehicle v " +
			"JOIN vs.company c " +
            "WHERE v.stage != :stage " +
            "AND vs.status = :status " +
			"AND c.companyType = com.montreal.msiav_bh.enumerations.CompanyTypeEnum.DADOS_DETRAN " +
	        "AND c.name =  'DETRAN - MS' ")
    List<SeizureNoticeDTO> findSeizureNoticeToSend(
            @Param("stage") VehicleStageEnum stage,
            @Param("status") SeizureStatusEnum status);
    
    
    @EntityGraph(attributePaths = {"vehicle", "company", "address", "user"})
    @Query("SELECT vs FROM VehicleSeizure vs WHERE vs.vehicle.id = :vehicleId ORDER BY vs.id ASC")
    Optional<VehicleSeizure> findFirstWithAllRelationsByVehicleId(@Param("vehicleId") Long vehicleId);
    
    
    @Query("""
	    SELECT vs 
	    FROM VehicleSeizure vs
	    JOIN FETCH vs.vehicle
	    JOIN FETCH vs.company
	    JOIN FETCH vs.address
	    JOIN FETCH vs.user
	    WHERE vs.id = :id
	""")
	Optional<VehicleSeizure> findByIdWithJoins(@Param("id") Long id);


}
