package com.montreal.oauth.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.montreal.oauth.domain.entity.Permission;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    List<Permission> findByParentIsNull();


    Optional<Permission> findByActionAndSubject(String action, String subject);
    
    @Query("SELECT f FROM Permission f WHERE f.id IN :ids")
    List<Permission> findByIdIn(List<Long> ids);
    
    @Query(value = "SELECT * FROM permissions WHERE id IN (:ids)", nativeQuery = true)
    List<Permission> findAllByIds(@Param("ids") List<Long> ids);
}
