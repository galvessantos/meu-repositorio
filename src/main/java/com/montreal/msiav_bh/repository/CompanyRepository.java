package com.montreal.msiav_bh.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.montreal.msiav_bh.entity.Company;
import com.montreal.msiav_bh.enumerations.CompanyTypeEnum;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long>, JpaSpecificationExecutor<Company> {

    @Query("SELECT c FROM Company c JOIN FETCH c.address WHERE c.id = :id")
    Optional<Company> findById(@Param("id") Long id);

    @Query("SELECT c FROM Company c WHERE c.companyType = :companyType")
    Optional<Company> findByCompanyType(@Param("companyType") CompanyTypeEnum companyType);

    @Query("SELECT c FROM Company c WHERE c.document = :document")
    Optional<Company> findByDocument(@Param("document") String document);

    @Query(value = "SELECT c.* FROM company c WHERE c.document = :document OR c.email = :email", nativeQuery = true)
    List<Company> pesquisar(@Param("document") String document, @Param("email") String email);

    @Query(value = """
    	    SELECT * FROM company c 
    	    WHERE 
    	        (:fieldValue IS NULL OR 
    	        CASE 
    	            WHEN :fieldName = 'document' THEN c.document 
    	            WHEN :fieldName = 'name' THEN c.name 
    	            WHEN :fieldName = 'email' THEN c.email 
    	            ELSE NULL 
    	        END ILIKE CONCAT('%', :fieldValue, '%'))
    	    """,
    	    nativeQuery = true)
    Page<Company> searchByDynamicField(
            @Param("fieldName") String fieldName,
            @Param("fieldValue") String fieldValue,
            Pageable pageable);

    boolean existsByEmail(String email);
    
    boolean existsByDocument(String document);

}
