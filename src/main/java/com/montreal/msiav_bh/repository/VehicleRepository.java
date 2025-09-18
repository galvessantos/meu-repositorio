package com.montreal.msiav_bh.repository;

import com.montreal.msiav_bh.entity.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

	@Query("SELECT v FROM Vehicle v WHERE v.contractNumber = :contractNumber")
	Optional<Vehicle> findByContractNumber(@Param("contractNumber") String contractNumber);

	@Query(value = """
			SELECT * FROM vehicle v
			WHERE
			    (:fieldValue IS NULL OR
			    CASE
			        WHEN :fieldName = 'licensePlate' THEN v.license_plate
			        WHEN :fieldName = 'brand' THEN v.brand
			        WHEN :fieldName = 'model' THEN v.model
			        WHEN :fieldName = 'color' THEN v.color
			        WHEN :fieldName = 'renavam' THEN v.renavam
			        WHEN :fieldName = 'chassi' THEN v.chassi
			        WHEN :fieldName = 'creditorName' THEN v.creditor_name
			        WHEN :fieldName = 'contractNumber' THEN v.contract_number
			        ELSE NULL
			    END ILIKE CONCAT('%', :fieldValue, '%'))
			""", nativeQuery = true)
	Page<Vehicle> searchByDynamicField(@Param("fieldName") String fieldName, @Param("fieldValue") String fieldValue,
			Pageable pageable);
}
