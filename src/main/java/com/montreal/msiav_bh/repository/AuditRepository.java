package com.montreal.msiav_bh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.montreal.msiav_bh.entity.RegisterAudit;

@Repository
public interface AuditRepository extends JpaRepository<RegisterAudit, Long> {

    @Query("SELECT ra FROM RegisterAudit ra WHERE ra.userId = :userId")
    List<RegisterAudit> findAllByUserId(@Param("userId") Long userId);
}
