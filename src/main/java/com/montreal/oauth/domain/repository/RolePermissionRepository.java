package com.montreal.oauth.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.montreal.oauth.domain.entity.Permission;
import com.montreal.oauth.domain.entity.Role;
import com.montreal.oauth.domain.entity.RolePermission;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
	
	boolean existsByRoleAndPermission(Role role, Permission permission);

    Optional<RolePermission>    findByRoleAndPermission(Role role, Permission permission);

    void deleteByRoleAndPermissionIn(Role role, Set<Permission> permissions);

    List<RolePermission> findByRole(Role role);

    @Transactional
    void deleteByRoleAndPermission(Role role, Permission permission);
    
}

