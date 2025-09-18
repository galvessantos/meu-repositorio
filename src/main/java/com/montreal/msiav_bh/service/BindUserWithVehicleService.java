package com.montreal.msiav_bh.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.montreal.core.domain.exception.NotFoundException;
import com.montreal.msiav_bh.dto.response.CompanyResponse;
import com.montreal.msiav_bh.entity.UserVehicleAssociation;
import com.montreal.msiav_bh.entity.Vehicle;
import com.montreal.msiav_bh.enumerations.CompanyTypeEnum;
import com.montreal.msiav_bh.repository.UserVehicleAssociationRepository;
import com.montreal.msiav_bh.repository.VehicleCompanyRepository;
import com.montreal.msiav_bh.repository.VehicleRepository;
import com.montreal.oauth.domain.entity.Role;
import com.montreal.oauth.domain.entity.UserInfo;
import com.montreal.oauth.domain.enumerations.RoleEnum;
import com.montreal.oauth.exception.BusinessException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BindUserWithVehicleService {

    private final CompanyService companyService;
    private final UserUtilsService userMongoService;
    private final VehicleRepository vehicleRepository;
    private final VehicleCompanyRepository vehicleCompanyRepository;
    private final UserVehicleAssociationRepository userVehicleAssociationRepository;

	public void bindUserAgenteOficialWithVehicleByCompany(Long companyId, Long vehicleId) {
		CompanyResponse company = companyService.findById(companyId);
		log.info("Empresa encontrada: {}", company);

		List<UserInfo> usuarios = userMongoService.findUsersByCompanyId(companyId);
		Vehicle vehicle = vehicleRepository.findById(vehicleId).orElseThrow(() -> new NotFoundException("Veículo não encontrado para ID: " + vehicleId));

		if (usuarios == null || usuarios.isEmpty()) {
			throw new BusinessException("Nenhum usuário encontrado para a empresa ID: " + companyId);
		}

		UserInfo userLogged = userMongoService.getLoggedInUser();

		if (!CompanyTypeEnum.DADOS_DETRAN.equals(company.getCompanyType())) {
			throw new BusinessException("Empresa não é do tipo DADOS_DETRAN.");
		}

		for (UserInfo user : usuarios) {
			validateUserForBinding(user, vehicleId);

			UserVehicleAssociation association = UserVehicleAssociation.builder()
					.user(user)
					.vehicle(vehicle)
					.associatedBy(userLogged)
					.createdAt(LocalDateTime.now())
					.build();

			userVehicleAssociationRepository.save(association);
			log.info("Vínculo entre usuário {} e veículo {} criado com sucesso.", user.getId(), vehicleId);
		}
	}

	public String bindUserWithVehicleAgenteOficial(Long userId, Long vehicleId, Long associatedBy) {
		
		Vehicle vehicle = vehicleRepository.findById(vehicleId).orElseThrow(() -> new NotFoundException("Veículo não encontrado para ID: " + vehicleId));
		UserInfo user = userMongoService.findById(userId);
		UserInfo userAssociatedBy = userMongoService.findById(associatedBy);

		validateUserAndVehicle(user, vehicle);
		validateUserForBinding(user, vehicleId);

		UserVehicleAssociation association = UserVehicleAssociation.builder()
				.user(user)
				.vehicle(vehicle)
				.associatedBy(userAssociatedBy)
				.createdAt(LocalDateTime.now())
				.build();

		userVehicleAssociationRepository.save(association);
		log.info("Vínculo entre usuário {} e veículo {} criado com sucesso.", userId, vehicleId);

		return "Usuário vinculado ao veículo com sucesso.";
	}

	public String unbindUserWithVehicleAgenteOficial(Long userId, Long vehicleId) {
		Vehicle vehicle = vehicleRepository.findById(vehicleId)
				.orElseThrow(() -> new NotFoundException("Veículo não encontrado para ID: " + vehicleId));
		UserInfo user = userMongoService.findById(userId);

		validateUserAndVehicle(user, vehicle);

		var association = userVehicleAssociationRepository.findByUserIdAndVehicleId(userId, vehicleId)
				.orElseThrow(() -> new NotFoundException("Vínculo entre usuário e veículo não encontrado."));

		userVehicleAssociationRepository.delete(association);
		log.info("Vínculo entre usuário {} e veículo {} removido com sucesso.", userId, vehicleId);

		return "Vínculo entre usuário e veículo removido com sucesso.";
	}

	public void bindUserAgenteAllVehicles(Long userId, Long companyId, Set<Role> roles) {
		CompanyResponse company = companyService.findById(companyId);
		List<Vehicle> vehicles = vehicleRepository.findAll();
		
		if (vehicles.isEmpty()) {
			throw new BusinessException("Não há veículos disponíveis para vinculação.");
		}
		if (Objects.isNull(companyId) || !CompanyTypeEnum.DADOS_DETRAN.equals(company.getCompanyType())) {
			throw new BusinessException("Empresa inválida ou não pertence ao tipo DADOS_DETRAN.");
		}

		log.info("Total de veículos encontrados: {}", vehicles.size());

		for (Vehicle vehicle : vehicles) {
			Long vehicleId = vehicle.getId();

			if (vehicleCompanyRepository.findByVehicleIdAndCompanyId(vehicleId, companyId).isPresent()) {
				validateUserForBinding(userId, vehicleId, roles);
				UserInfo user = userMongoService.findById(userId);
				UserVehicleAssociation association = UserVehicleAssociation.builder()
						.user(user)
						.vehicle(vehicle)
						.associatedBy(user)
						.createdAt(LocalDateTime.now())
						.build();

				userVehicleAssociationRepository.save(association);
				log.info("Vínculo entre usuário {} e veículo {} criado com sucesso.", userId, vehicleId);
			}
		}
	}

	private void validateUserAndVehicle(UserInfo user, Vehicle vehicle) {
		if (vehicle == null) {
			throw new NotFoundException("Veículo não encontrado.");
		}
		log.info("Veículo encontrado: {}", vehicle);

		if (user == null) {
			throw new NotFoundException("Usuário não encontrado.");
		}
		log.info("Usuário encontrado: {}", user);
	}

	private void validateUserForBinding(UserInfo user, Long vehicleId) {
		if (!isAgenteOficial(user)) {
			throw new BusinessException("Usuário " + user.getId() + " não é um agente oficial.");
		}

		if (userVehicleAssociationRepository.existsByUserIdAndVehicleId(user.getId(), vehicleId)) {
			throw new BusinessException("Já existe um vínculo entre o usuário " + user.getId() + " e o veículo " + vehicleId + ".");
		}
	}

	private void validateUserForBinding(Long userId, Long vehicleId, Set<Role> roles) {
		if (!isAgenteOficial(roles)) {
			throw new BusinessException("Usuário " + userId + " não é um agente oficial.");
		}

		if (userVehicleAssociationRepository.existsByUserIdAndVehicleId(userId, vehicleId)) {
			throw new BusinessException("Já existe um vínculo entre o usuário " + userId + " e o veículo " + vehicleId + ".");
		}
	}

	private boolean isAgenteOficial(UserInfo user) {
		return user.getRoles().stream()
				.anyMatch(role -> RoleEnum.ROLE_AGENTE_OFICIAL.equals(role.getName()));
	}

	private boolean isAgenteOficial(Set<Role> roles) {
		if (roles == null || roles.isEmpty()) {
			return false;
		}

		return roles.stream()
				.anyMatch(role -> RoleEnum.ROLE_AGENTE_OFICIAL.equals(role.getName()));
	}

}
