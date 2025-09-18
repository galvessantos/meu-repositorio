package com.montreal.msiav_bh.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.montreal.msiav_bh.entity.Company;

public interface CompanyPRepository extends JpaRepository<Company, Long> {
	
}
