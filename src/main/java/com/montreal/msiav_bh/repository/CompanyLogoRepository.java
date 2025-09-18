package com.montreal.msiav_bh.repository;

import java.util.Optional;

import com.montreal.msiav_bh.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.montreal.msiav_bh.entity.CompanyLogo;

@Repository
public interface CompanyLogoRepository extends JpaRepository<CompanyLogo, Long> {

    Optional<CompanyLogo> findByCompanyId(Long companyId);

    Optional<CompanyLogo> findByCompany(Company company);
}
