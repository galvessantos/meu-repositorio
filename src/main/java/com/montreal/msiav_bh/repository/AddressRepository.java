package com.montreal.msiav_bh.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.montreal.msiav_bh.entity.Address;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

	@Query(value = """
	    SELECT * FROM address 
	    WHERE postal_code = :postalCode 
	      AND street = :street 
	      AND number = :number 
	      AND neighborhood = :neighborhood 
	      AND city = :city
	    """, nativeQuery = true)
	List<Address> findAddressByFields(
	    @Param("postalCode") String postalCode,
	    @Param("street") String street,
	    @Param("number") String number,
	    @Param("neighborhood") String neighborhood,
	    @Param("city") String city
	);
	
    @Query(value = """
        SELECT * FROM address a 
        WHERE 
            (:fieldValue IS NULL OR 
            CASE 
                WHEN :fieldName = 'postal_code' THEN a.postal_code 
                WHEN :fieldName = 'street' THEN a.street 
                WHEN :fieldName = 'number' THEN a.number 
                WHEN :fieldName = 'neighborhood' THEN a.neighborhood 
                WHEN :fieldName = 'city' THEN a.city 
                WHEN :fieldName = 'state' THEN a.state 
                ELSE NULL 
            END ILIKE CONCAT('%', :fieldValue, '%'))
        """,
        nativeQuery = true)
    Page<Address> searchByDynamicField(
            @Param("fieldName") String fieldName,
            @Param("fieldValue") String fieldValue,
            Pageable pageable);
    
    
    @Query("""
        SELECT a FROM Address a 
        WHERE a.postalCode = :postalCode 
          AND a.street = :street 
          AND a.number = :number 
          AND a.neighborhood = :neighborhood 
          AND a.city = :city
    """)
    Optional<Address> findExistingAddress(
        @Param("postalCode") String postalCode,
        @Param("street") String street,
        @Param("number") String number,
        @Param("neighborhood") String neighborhood,
        @Param("city") String city
    );
    
    Optional<Address> findByPostalCodeAndStreetAndNumberAndNeighborhoodAndCity(
    	    String postalCode,
    	    String street,
    	    String number,
    	    String neighborhood,
    	    String city
    	);

}
