package com.montreal.msiav_bh.mapper;

import com.montreal.msiav_bh.dto.VehicleDTO;
import com.montreal.msiav_bh.entity.VehicleCache;
import com.montreal.msiav_bh.service.VehicleCacheCryptoService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public abstract class VehicleCacheMapper {

    @Autowired
    protected VehicleCacheCryptoService cryptoService;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "externalId", source = "dto.id")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "apiSyncDate", source = "syncDate")
    @Mapping(target = "placa", source = "dto.placa")
    @Mapping(target = "contrato", source = "dto.contrato")
    @Mapping(target = "cpfHash", ignore = true)
    @Mapping(target = "protocoloHash", ignore = true)
    @Mapping(target = "contratoHash", ignore = true)
    @Mapping(target = "placaHash", ignore = true)
    @Mapping(target = "contratoPlacaHash", ignore = true)
    @Mapping(target = "marcaModelo", ignore = true)
    @Mapping(target = "registroDetran", ignore = true)
    @Mapping(target = "possuiGPS", ignore = true)
    @Mapping(target = "anoFabricacao", ignore = true)
    @Mapping(target = "anoModelo", ignore = true)
    @Mapping(target = "cor", ignore = true)
    @Mapping(target = "chassi", ignore = true)
    @Mapping(target = "renavam", ignore = true)
    @Mapping(target = "gravame", ignore = true)
    @Mapping(target = "contratoEntity", ignore = true)
    public abstract VehicleCache toEntity(VehicleDTO dto, LocalDateTime syncDate);

    @Mapping(target = "id", source = "externalId")
    @Mapping(target = "placa", expression = "java(cryptoService.decryptPlaca(entity.getPlaca()))")
    @Mapping(target = "contrato", expression = "java(cryptoService.decryptContrato(entity.getContrato()))")
    public abstract VehicleDTO toDTO(VehicleCache entity);


}