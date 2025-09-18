package com.montreal.oauth.domain.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.montreal.oauth.domain.dto.PermissionDTO;
import com.montreal.oauth.domain.dto.response.PermissionResponseDTO;
import com.montreal.oauth.domain.entity.Permission;
import com.montreal.oauth.domain.entity.Role;
import com.montreal.oauth.domain.entity.RolePermission;
import com.montreal.oauth.domain.exception.DuplicateResourceException;
import com.montreal.oauth.domain.exception.ResourceNotFoundException;
import com.montreal.oauth.domain.mapper.PermissionMapper;
import com.montreal.oauth.domain.repository.PermissionRepository;
import com.montreal.oauth.domain.repository.RolePermissionRepository;
import com.montreal.oauth.domain.repository.RoleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionService {

	private final RoleRepository roleRepository;
    private final PermissionMapper permissionMapper;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    public List<Permission> findAll() {
        return permissionRepository.findAll();
    }

    public PermissionResponseDTO findByActionAndSubject(String action, String subject) {
        Permission permission = permissionRepository.findByActionAndSubject(action, subject)
                .orElseThrow(() -> new ResourceNotFoundException("Permissão não encontrada para ação: " + action + " e sujeito: " + subject));
        return permissionMapper.toResponseDTO(permission);
    }

    public Permission create(PermissionDTO dto) {
        log.info("Verificando se a permissão '{}' para '{}' já existe...", dto.getAction(), dto.getSubject());

        Optional<Permission> existingPermission = permissionRepository.findByActionAndSubject(dto.getAction(), dto.getSubject());
        if (existingPermission.isPresent()) {
            throw new DuplicateResourceException("Já existe uma permissão com a ação '" + dto.getAction() + "' e sujeito '" + dto.getSubject() + "'.");
        }

        Permission permission = permissionMapper.toEntity(dto);
        return permissionRepository.save(permission);
    }

    public Permission update(Long id, PermissionDTO dto) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permissão não encontrada com ID: " + id));

        // Verifica se já existe outra permissão com os mesmos dados antes de atualizar
        Optional<Permission> existingPermission = permissionRepository.findByActionAndSubject(dto.getAction(), dto.getSubject());
        if (existingPermission.isPresent() && !existingPermission.get().getId().equals(id)) {
            throw new DuplicateResourceException("Já existe uma permissão com a ação '" + dto.getAction() + "' e sujeito '" + dto.getSubject() + "'.");
        }

        permission.setAction(dto.getAction());
        permission.setSubject(dto.getSubject());
        permission.setFields(dto.getFields());
        permission.setDescription(dto.getDescription());
        return permissionRepository.save(permission);
    }

    public void deleteById(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permissão não encontrada com ID: " + id));

        permissionRepository.delete(permission);
    }
    
    //===

    @Transactional
    public void associatePermissionToRole(Integer roleId, Long permissionId) {
        log.info("Verificando se a role ID {} e a permissão ID {} existem para associar...", roleId, permissionId);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role não encontrada com ID: " + roleId));

        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permissão não encontrada com ID: " + permissionId));

        // Verifica se a associação já existe
        if (rolePermissionRepository.existsByRoleAndPermission(role, permission)) {
            throw new DuplicateResourceException("A permissão já está associada a esta role.");
        }

        RolePermission rolePermission = new RolePermission(role, permission);
        rolePermissionRepository.save(rolePermission);
    }

    @Transactional
    public void disassociatePermissionFromRole(Integer roleId, Long permissionId) {
        log.info("Verificando se a role ID {} e a permissão ID {} existem para remover o vínculo...", roleId, permissionId);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role não encontrada com ID: " + roleId));

        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permissão não encontrada com ID: " + permissionId));

        // Verifica se a associação existe
        Optional<RolePermission> rolePermission = rolePermissionRepository.findByRoleAndPermission(role, permission);
        
        if (rolePermission.isEmpty()) {
            throw new ResourceNotFoundException("A permissão não está associada a esta role.");
        }

        rolePermissionRepository.delete(rolePermission.get());
    }
    
    @Transactional
    public void associatePermissionsToRole(Integer roleId, List<Long> permissionIds) {
        log.info("Atualizando permissões para a Role ID {} com as permissões {}", roleId, permissionIds);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role não encontrada com ID: " + roleId));


        List<Permission> permissions = permissionRepository.findAllById(permissionIds);


        if (permissions.size() != permissionIds.size()) {
            throw new ResourceNotFoundException("Uma ou mais permissões não foram encontradas.");
        }


        Set<Permission> newPermissions = new HashSet<>(permissions);


        List<RolePermission> existingRolePermissions = rolePermissionRepository.findByRole(role);


        Set<Permission> currentPermissions = existingRolePermissions.stream()
                .map(RolePermission::getPermission)
                .collect(Collectors.toSet());


        Set<Permission> permissionsToRemove = new HashSet<>(currentPermissions);
        permissionsToRemove.removeAll(newPermissions);

        Set<Permission> permissionsToAdd = new HashSet<>(newPermissions);
        permissionsToAdd.removeAll(currentPermissions);


        if (!permissionsToRemove.isEmpty()) {
            rolePermissionRepository.deleteByRoleAndPermissionIn(role, permissionsToRemove);
        }

        // Adiciona novas permissões que ainda não estavam associadas
        List<RolePermission> newRolePermissions = permissionsToAdd.stream()
                .map(permission -> new RolePermission(role, permission))
                .collect(Collectors.toList());

        rolePermissionRepository.saveAll(newRolePermissions);
    }


    @Transactional
    public void disassociatePermissionsFromRole(Integer roleId, List<Long> permissionIds) {
        log.info("Verificando se a role ID {} existe para remover permissões {}", roleId, permissionIds);

        Role role = roleRepository.findById(roleId).orElseThrow(() -> new ResourceNotFoundException("Role não encontrada com ID: " + roleId));

        List<Permission> permissions = permissionRepository.findAllById(permissionIds);

        if (permissions.size() != permissionIds.size()) {
            throw new ResourceNotFoundException("Uma ou mais permissões não foram encontradas.");
        }

        for (Permission permission : permissions) {
            Optional<RolePermission> rolePermission = rolePermissionRepository.findByRoleAndPermission(role, permission);

            if (rolePermission.isPresent()) {
                rolePermissionRepository.delete(rolePermission.get());
                log.info("Permissão ID {} removida da role ID {}", permission.getId(), roleId);
            } else {
                log.warn("A permissão ID {} não estava associada à role ID {}", permission.getId(), roleId);
            }
        }
    }
    
}
