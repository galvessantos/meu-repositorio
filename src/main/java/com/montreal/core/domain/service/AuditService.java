package com.montreal.core.domain.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.montreal.core.domain.dto.RegisterAuditDTO;
import com.montreal.core.interceptor.ClientIpHolder;
import com.montreal.msiav_bh.mapper.IAuditMapper;
import com.montreal.msiav_bh.repository.AuditRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditRepository auditRepository;

    public void registerAudit(RegisterAuditDTO registerAuditDTO) {

        log.info("Registrando auditoria: {}", registerAuditDTO);

        var audit = IAuditMapper.INSTANCE.toEntity(registerAuditDTO);

        log.info("Recuperando ip de acesso");
        String clientIp = ClientIpHolder.getClientIp();
        audit.setIpAddress(clientIp);
        audit.setTimestamp(LocalDateTime.now());

        log.info("Salvando auditoria");
        var auditSaved = auditRepository.save(audit);

        log.info("Auditoria salva: {}", auditSaved);

    }

    public List<RegisterAuditDTO> listAll() {
        log.info("Listando todas as auditorias");
        return IAuditMapper.INSTANCE.toCollectionDTO(auditRepository.findAll());
    }

    public List<RegisterAuditDTO> listAllByUserId(Long userId) {
        log.info("Listando todas as auditorias por userId: {}", userId);
        return IAuditMapper.INSTANCE.toCollectionDTO(auditRepository.findAllByUserId(userId));
    }

}
