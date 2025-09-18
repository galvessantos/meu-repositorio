package com.montreal.oauth.domain.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import com.montreal.oauth.domain.entity.RolePermission;
import com.montreal.oauth.domain.repository.RolePermissionRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RolePermissionService {

    private final RolePermissionRepository rolePermissionRepository;

    public List<RolePermission> findAll() {
        return rolePermissionRepository.findAll();
    }

    public RolePermission saveRolePermission(RolePermission rolePermission) {return rolePermissionRepository.save(rolePermission);
    }

    public Optional<RolePermission> findById(Long id) {return rolePermissionRepository.findById(id);}

    public void deleteById(Long id) {rolePermissionRepository.deleteById(id);}
}

