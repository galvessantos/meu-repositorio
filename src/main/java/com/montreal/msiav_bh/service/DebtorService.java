package com.montreal.msiav_bh.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.montreal.core.domain.exception.NotFoundException;
import com.montreal.core.utils.CryptoUtil;
import com.montreal.msiav_bh.dto.request.DebtorRequest;
import com.montreal.msiav_bh.dto.response.DebtorResponse;
import com.montreal.msiav_bh.entity.Address;
import com.montreal.msiav_bh.entity.Debtor;
import com.montreal.msiav_bh.mapper.AddressMapper;
import com.montreal.msiav_bh.mapper.DebtorMapper;
import com.montreal.msiav_bh.repository.AddressRepository;
import com.montreal.msiav_bh.repository.DebtorRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DebtorService {

    private final CryptoUtil cryptoUtil;
    private final DebtorMapper debtorMapper;
    private final AddressMapper addressMapper;
    private final DebtorRepository debtorRepository;
    private final AddressRepository addressRepository;

    public DebtorResponse createDebtor(DebtorRequest request) {
        try {
            log.info("Criando devedor: {}", request.getName());
            Address address = addressRepository.save(addressMapper.toEntity(request.getAddress()));

            Debtor debtor = new Debtor();
            debtor.setName(request.getName());
            debtor.setCpfCnpj(request.getCpfCnpj());
            debtor.setAddress(address);

            debtorRepository.saveEncrypted(debtor);

            DebtorResponse response = debtorMapper.toResponse(debtor);
            response.setCpfCnpj(maskCpf(debtor.getCpfCnpj()));

            log.info("Devedor criado com sucesso. ID: {}", debtor.getId());
            return response;
        } catch (Exception e) {
            log.error("Erro ao criar devedor", e);
            throw new RuntimeException("Erro interno ao criar devedor.");
        }
    }

    public DebtorResponse updateDebtor(Long id, DebtorRequest request) {
        try {
            log.info("Atualizando devedor ID: {}", id);
            Debtor existingDebtor = debtorRepository.findDecryptedById(id)
                    .orElseThrow(() -> new NotFoundException("Devedor não encontrado com ID: " + id));

            existingDebtor.setName(request.getName());
            existingDebtor.setCpfCnpj(request.getCpfCnpj());

            if (request.getAddress() != null) {
                Address updatedAddress = addressMapper.toEntity(request.getAddress());
                updatedAddress.setId(existingDebtor.getAddress().getId());
                Address savedAddress = addressRepository.save(updatedAddress);
                existingDebtor.setAddress(savedAddress);
            }

            debtorRepository.saveEncrypted(existingDebtor);

            DebtorResponse response = debtorMapper.toResponse(existingDebtor);
            response.setCpfCnpj(maskCpf(existingDebtor.getCpfCnpj()));
            log.info("Devedor atualizado com sucesso. ID: {}", id);
            return response;
        } catch (NotFoundException e) {
            log.warn("Devedor não encontrado para atualização. ID: {}", id);
            throw e;
        } catch (Exception e) {
            log.error("Erro ao atualizar devedor ID: {}", id, e);
            throw new RuntimeException("Erro interno ao atualizar devedor.");
        }
    }

    public List<DebtorResponse> getAllDebtors() {
        log.info("Buscando todos os devedores");
        List<Debtor> debtors = debtorRepository.findAllDecrypted();
        List<DebtorResponse> responses = new ArrayList<>();

        for (Debtor debtor : debtors) {
            try {
                DebtorResponse response = debtorMapper.toResponse(debtor);
                response.setCpfCnpj(maskCpf(debtor.getCpfCnpj()));
                responses.add(response);
            } catch (Exception e) {
                log.error("Erro ao processar devedor com ID {}", debtor.getId(), e);
            }
        }

        return responses;
    }

    public DebtorResponse getDebtorById(Long id) {
        try {
            log.info("Buscando devedor por ID: {}", id);
            Debtor debtor = debtorRepository.findDecryptedById(id)
                    .orElseThrow(() -> new NotFoundException("Devedor não encontrado com ID: " + id));

            DebtorResponse response = debtorMapper.toResponse(debtor);
            response.setCpfCnpj(maskCpf(debtor.getCpfCnpj()));
            return response;
        } catch (NotFoundException e) {
            log.warn("Devedor não encontrado. ID: {}", id);
            throw e;
        } catch (Exception e) {
            log.error("Erro ao buscar devedor por ID: {}", id, e);
            throw new RuntimeException("Erro interno ao buscar devedor.");
        }
    }

    public DebtorResponse getDebtorFullById(Long id) {
        try {
            log.info("Buscando devedor completo por ID: {}", id);
            Debtor debtor = debtorRepository.findDecryptedById(id)
                    .orElseThrow(() -> new NotFoundException("Devedor não encontrado com ID: " + id));
            return debtorMapper.toResponse(debtor);
        } catch (NotFoundException e) {
            log.warn("Devedor não encontrado (full). ID: {}", id);
            throw e;
        } catch (Exception e) {
            log.error("Erro ao buscar devedor completo por ID: {}", id, e);
            throw new RuntimeException("Erro interno ao buscar devedor.");
        }
    }

    public Page<Debtor> searchWithFilters(String fieldName, String fieldValue, Pageable pageable) {
        try {
            log.info("Buscando devedores pelo campo {} com valor: {}", fieldName, fieldValue);

            if (fieldName == null || fieldValue == null || fieldValue.isBlank()) {
                log.info("Nenhum filtro informado, retornando todos os devedores.");
                return debtorRepository.findAll(pageable);
            }

            return debtorRepository.searchByDynamicField(fieldName, fieldValue, pageable);
        } catch (Exception e) {
            log.error("Erro ao buscar devedores com filtro", e);
            throw new RuntimeException("Erro interno ao buscar devedores.");
        }
    }

    private String maskCpf(String cpf) {
        if (cpf == null || cpf.length() < 4) return "***";
        if (cpf.length() == 11) {
            return "***.***.***-" + cpf.substring(9);
        } else if (cpf.length() == 14) {
            return "**.***.***/****-" + cpf.substring(12);
        }
        return "***" + cpf.substring(cpf.length() - 2);
    }
}
