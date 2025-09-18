package com.montreal.msiav_bh.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.montreal.core.domain.exception.InternalErrorException;
import com.montreal.core.domain.exception.InvalidParameterException;
import com.montreal.core.domain.exception.NotFoundException;
import com.montreal.msiav_bh.dto.response.CompanyResponse;
import com.montreal.msiav_bh.dto.response.UserVehicleAssociationResponse;
import com.montreal.msiav_bh.dto.response.VehicleResponse;
import com.montreal.msiav_bh.entity.UserVehicleAssociation;
import com.montreal.msiav_bh.entity.Vehicle;
import com.montreal.msiav_bh.repository.UserVehicleAssociationRepository;
import com.montreal.oauth.domain.dto.response.UserResponse;
import com.montreal.oauth.domain.entity.UserInfo;
import com.montreal.oauth.domain.enumerations.RoleEnum;
import com.montreal.oauth.domain.repository.IUserRepository;
import com.montreal.oauth.domain.service.UserService;
import com.montreal.oauth.mapper.IUserMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OfficialAgentService {

	private final UserService userService;
    private final VehicleService vehicleService;
    private final CompanyService companyService;
    private final IUserRepository userRepository;
    private final UserUtilsService userUtilsService;
    private final UserVehicleAssociationRepository userVehicleAssociationRepository;

    public List<VehicleResponse> veiculosDoAgenteOficial(Long userId) {
        log.info("Buscando veículos para o agente oficial com ID {}", userId);
        try {
            List<VehicleResponse> lista = new ArrayList<>();
            boolean idAF = isAgenteOficial(userId);
            if (idAF) {
                List<UserVehicleAssociation> vinculos = userVehicleAssociationRepository.findByUserId(userId);
                for (UserVehicleAssociation vinculo : vinculos) {
                    Long vehicleId = vinculo.getVehicle().getId();
                    VehicleResponse vr = vehicleService.findVehicleById(vehicleId);
                    lista.add(vr);
                }
                log.info("Veículos encontrados para o agente oficial com ID {}: {}", userId, lista);
                return lista;
            }
            log.warn("Usuário com ID {} não é um agente oficial.", userId);
            return null;
        } catch (Exception e) {
            log.error("Erro ao buscar veículos para o agente oficial com ID {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    public boolean afTemVeiculo(Long userId) {
        log.info("Verificando se o agente oficial com ID {} possui veículos associados", userId);
        try {
            boolean isAF = isAgenteOficial(userId);
            if (isAF) {
                List<UserVehicleAssociation> vinculos = userVehicleAssociationRepository.findByUserId(userId);
                boolean resultado = vinculos != null && !vinculos.isEmpty();
                log.info("Agente oficial com ID {} possui veículos associados: {}", userId, resultado);
                return resultado;
            }
            log.warn("Usuário com ID {} não é um agente oficial.", userId);
            return false;
        } catch (Exception e) {
            log.error("Erro ao verificar veículos para o agente oficial com ID {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }
    
    public List<Long> usuariosDoVeiculo(Long vehicleId) {
        log.info("Buscando usuários vinculados ao veículo {}", vehicleId);
        try {
            List<Long> usuarios = new ArrayList<>();
            List<UserVehicleAssociation> vinculos = userVehicleAssociationRepository.findAllByVehicleId(vehicleId);
            if (vinculos != null && !vinculos.isEmpty()) {
                for (UserVehicleAssociation vinculo : vinculos) {
                    usuarios.add(vinculo.getUser().getId());
                }
                log.info("Usuários encontrados para o veículo {}: {}", vehicleId, usuarios);
                return usuarios;
            }
            log.warn("Nenhum usuário encontrado para o veículo {}", vehicleId);
            return null;
        } catch (Exception e) {
            log.error("Erro ao buscar usuários para o veículo {}: {}", vehicleId, e.getMessage(), e);
            throw e;
        }
    }

    public boolean isAgenteOficial(Long idUser) {
        log.info("Verificando se o usuário com ID {} é um agente oficial", idUser);
        try {
            UserInfo user = userUtilsService.findById(idUser);
            boolean isAgente = user.getRoles().stream().anyMatch(role -> RoleEnum.ROLE_AGENTE_OFICIAL.equals(role.getName()));
            log.info("Usuário com ID {} é agente oficial: {}", idUser, isAgente);
            return isAgente;
        } catch (Exception e) {
            log.error("Erro ao verificar se o usuário com ID {} é um agente oficial: {}", idUser, e.getMessage(), e);
            return false;
        }
    }
    
    public List<UserInfo> usuariosDaEmpresa(Long companyId) {
        log.info("Buscando usuários vinculados à empresa com ID {}", companyId);
        try {
            List<UserInfo> usuarios = userUtilsService.findUsersByCompanyId(companyId);
            CompanyResponse company = companyService.findById(companyId);
            List<UserInfo> listaAf = new ArrayList<>();

            if (company == null) {
                log.error("Empresa com ID {} não encontrada", companyId);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Empresa com ID " + companyId + " não existe!");
            }

            if (usuarios != null && !usuarios.isEmpty()) {
                log.info("Usuários encontrados vinculados à empresa com ID {}: {}", companyId, usuarios);
                for (UserInfo user : usuarios) {
                    boolean isAgente = user.getRoles().stream()
                            .anyMatch(role -> RoleEnum.ROLE_AGENTE_OFICIAL.equals(role.getName()));

                    if (isAgente) {
                        listaAf.add(user);
                    }
                }
            } else {
                log.warn("Nenhum usuário encontrado para a empresa com ID {}", companyId);
            }

            log.info("Agentes oficiais encontrados para a empresa com ID {}: {}", companyId, listaAf);
            return listaAf;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao buscar usuários para a empresa com ID {}: {}", companyId, e.getMessage(), e);
            throw new InternalErrorException("Erro interno ao processar a solicitação.");
        }
    }
    
    public List<UserResponse> mapResponseAgenteOficial(List<UserInfo> usuarios) {
    	List<UserResponse> agentesOficiais = new ArrayList<>();
    	if(usuarios != null && !usuarios.isEmpty()) {
	    	for(UserInfo user: usuarios) {
	    		var userResponse = IUserMapper.INSTANCE.toResponse(userService.decryptSensitiveFields(user));
	    		agentesOficiais.add(userResponse);
	    	}
    	}
    	return agentesOficiais;
    }

    public List<UserResponse> mapResponseAgenteOficialWithoutDecrypt(List<UserInfo> usuarios) {
    	List<UserResponse> agentesOficiais = new ArrayList<>();
    	if(usuarios != null && !usuarios.isEmpty()) {
    		for(UserInfo user: usuarios) {
    			var userResponse = IUserMapper.INSTANCE.toResponse(user);
    			agentesOficiais.add(userResponse);
    		}
    	}
    	return agentesOficiais;
    }

    public UserResponse mapResponseAgenteOficial2(UserInfo agenteOficial) {
        UserResponse response = new UserResponse();

        response.setId(agenteOficial.getId());
        response.setUsername(agenteOficial.getUsername());
        response.setFullName(agenteOficial.getFullName());
        response.setEmail(agenteOficial.getEmail());
        response.setEnabled(agenteOficial.isEnabled());
        response.setLink(agenteOficial.getLink());
        response.setReset(agenteOficial.isReset());
        response.setResetAt(agenteOficial.getResetAt());
        response.setCreatedByAdmin(agenteOficial.isCreatedByAdmin());
        response.setPasswordChangedByUser(agenteOficial.isPasswordChangedByUser());
        response.setCompanyId(agenteOficial.getCompanyId());
        response.setPhone(agenteOficial.getPhone());
        response.setCpf(agenteOficial.getCpf());
        return response;
    }

    public List<UserInfo> findAgentesByVehicleIdAndType(Long vehicleId) {
        List<UserVehicleAssociation> associations = userVehicleAssociationRepository.findAllByVehicleId(vehicleId);

        List<Long> userIds = associations.stream()
                .map(association -> association.getUser().getId())
                .collect(Collectors.toList());

        List<UserInfo> users = userRepository.findAllById(userIds);
        
        return users.stream()
                .map(userService::decryptSensitiveFields)
                .collect(Collectors.toList());
    }
    
    
    public UserVehicleAssociationResponse vincularVeiculoAgenteOficial(Long vehicleId, Long userId) {
        log.info("Iniciando vinculação do agente oficial ID={} ao veículo ID={}", userId, vehicleId);

        try {
            if (vehicleId == null || userId == null) {
                throw new InvalidParameterException("vehicleId e userId são obrigatórios.");
            }

            Vehicle vehicle = vehicleService.findById(vehicleId);
            UserInfo user = userUtilsService.findById(userId);
            user = userService.decryptSensitiveFields(user);
            UserInfo usuarioLogado = userUtilsService.getAuthenticatedUserInfo();

            Optional<UserVehicleAssociation> existingAssociation = userVehicleAssociationRepository.findByVehicleId(vehicleId);

            UserVehicleAssociation association;
            if (existingAssociation.isPresent()) {
                association = existingAssociation.get();
                association.setUser(user);
                association.setAssociatedBy(usuarioLogado);
                association.setCreatedAt(LocalDateTime.now());

                userVehicleAssociationRepository.save(association);
                log.info("Vínculo existente atualizado com sucesso para veículo ID={}", vehicleId);
            } else {
                association = UserVehicleAssociation.builder()
                        .vehicle(vehicle)
                        .user(user)
                        .associatedBy(usuarioLogado)
                        .createdAt(LocalDateTime.now())
                        .build();

                userVehicleAssociationRepository.save(association);
                log.info("Novo vínculo criado com sucesso para veículo ID={} e usuário ID={}", vehicleId, userId);
            }

            return UserVehicleAssociationResponse.builder()
                    .id(association.getId())
                    .userId(association.getUser().getId())
                    .vehicleId(association.getVehicle().getId())
                    .associatedById(association.getAssociatedBy().getId())
                    .createdAt(association.getCreatedAt())
                    .build();

        } catch (InvalidParameterException | NotFoundException e) {
            log.warn("Erro de validação: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erro interno ao vincular veículo ID={} ao agente oficial ID={}: {}", vehicleId, userId, e.getMessage(), e);
            throw new InternalErrorException("Erro ao processar vínculo entre veículo e agente oficial.");
        }
    }

}

