package com.montreal.msiav_bh.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.montreal.msiav_bh.entity.FileReport;

public interface FileReportRepository extends JpaRepository<FileReport, Long> {

	@Query("SELECT f FROM FileReport f WHERE f.report.id = :reportId")
	Optional<FileReport> findByReportId(@Param("reportId") Long reportId);

}
