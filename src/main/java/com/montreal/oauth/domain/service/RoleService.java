package com.montreal.oauth.domain.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.montreal.core.domain.exception.NegocioException;
import com.montreal.oauth.domain.dto.RoleDTO;
import com.montreal.oauth.domain.entity.Role;
import com.montreal.oauth.domain.entity.RoleFunctionality;
import com.montreal.oauth.domain.entity.RolePermission;
import com.montreal.oauth.domain.mapper.RoleMapper;
import com.montreal.oauth.domain.repository.FunctionalityRepository;
import com.montreal.oauth.domain.repository.PermissionRepository;
import com.montreal.oauth.domain.repository.RoleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final FunctionalityRepository functionalityRepository;

    @Transactional
    public RoleDTO createRole(RoleDTO roleDTO) {
        log.info("Criando role: {}", roleDTO.getName());

        if (roleRepository.findByName(roleDTO.getName()).isPresent()) {
            throw new NegocioException(String.format("Role %s já existe", roleDTO.getName()));
        }

        Role role = RoleMapper.INSTANCE.toEntity(roleDTO);

        try {
            // Associa permissões à Role
            if (roleDTO.getPermissionIds() != null) {
                role.setRolePermissions(
                        roleDTO.getPermissionIds().stream()
                                .map(permissionId -> new RolePermission(role, permissionRepository.findById(permissionId)
                                        .orElseThrow(() -> new NegocioException("Permissão não encontrada"))))
                                .collect(Collectors.toSet())
                );
            }

            // Associa funcionalidades à Role
            if (roleDTO.getFunctionalityIds() != null) {
                role.setRoleFunctionalities(
                        roleDTO.getFunctionalityIds().stream()
                                .map(functionalityId -> new RoleFunctionality(role, functionalityRepository.findById(functionalityId)
                                        .orElseThrow(() -> new NegocioException("Funcionalidade não encontrada"))))
                                .collect(Collectors.toSet())
                );
            }

            Role savedRole = roleRepository.save(role);
            log.info("Role {} criada com sucesso!", roleDTO.getName());

            return RoleMapper.INSTANCE.toDTO(savedRole);
        } catch (Exception e) {
            log.error("Erro ao criar role: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao criar role");
        }
    }

    @Transactional(readOnly = true)
    public List<RoleDTO> getAllRoles() {
        log.info("Buscando todas as roles.");
        return RoleMapper.INSTANCE.toDTOList(roleRepository.findAll());
    }

    public RoleDTO getRoleById(Integer id) {
        log.info("Buscando role com ID: {}", id);
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new NegocioException("Role não encontrada para o ID: " + id));
        return RoleMapper.INSTANCE.toDTO(role);
    }

    @Transactional
    public RoleDTO updateRolePermissions(Integer roleId, List<Long> permissionIds) {
        log.info("Atualizando permissões da role ID: {}", roleId);
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new NegocioException("Role não encontrada"));

        // Remove permissões antigas antes de adicionar as novas
        role.getRolePermissions().clear();

        role.setRolePermissions(permissionIds.stream()
                .map(permissionId -> new RolePermission(role, permissionRepository.findById(permissionId)
                        .orElseThrow(() -> new NegocioException("Permissão não encontrada"))))
                .collect(Collectors.toSet()));

        return RoleMapper.INSTANCE.toDTO(roleRepository.save(role));
    }

    @Transactional
    public RoleDTO updateRoleFunctionalities(Integer roleId, List<Long> functionalityIds) {
        log.info("Atualizando funcionalidades da role ID: {}", roleId);
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new NegocioException("Role não encontrada"));

        // Remove funcionalidades antigas antes de adicionar as novas
        role.getRoleFunctionalities().clear();

        role.setRoleFunctionalities(functionalityIds.stream()
                .map(functionalityId -> new RoleFunctionality(role, functionalityRepository.findById(functionalityId)
                        .orElseThrow(() -> new NegocioException("Funcionalidade não encontrada"))))
                .collect(Collectors.toSet()));

        return RoleMapper.INSTANCE.toDTO(roleRepository.save(role));
    }

    @Transactional
    public void deleteRole(Integer roleId) {
        log.info("Deletando role ID: {}", roleId);
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new NegocioException("Role não encontrada"));

        // Remove todas as permissões e funcionalidades antes de deletar a Role
        role.getRolePermissions().clear();
        role.getRoleFunctionalities().clear();

        roleRepository.delete(role);
        log.info("Role ID {} deletada com sucesso.", roleId);
    }
}
