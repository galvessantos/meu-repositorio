package com.montreal.msiav_bh.service;

import com.montreal.core.utils.PostgresCryptoUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleCacheCryptoService {

    private final PostgresCryptoUtil postgresCryptoUtil;

    public String encryptPlaca(String placa) {
        if (placa == null || placa.trim().isEmpty() || "N/A".equals(placa)) {
            return placa;
        }

        String placaTrimmed = placa.toUpperCase().trim();

        try {
            String encrypted = postgresCryptoUtil.encrypt(placaTrimmed);

            if (encrypted != null && encrypted.length() >= 50 && encrypted.matches("[a-fA-F0-9]+")) {
                log.debug("Placa '{}' criptografada com PostgresCryptoUtil: {} chars", placa, encrypted.length());
                return encrypted;
            } else {
                log.error("PostgresCryptoUtil retornou valor suspeito para placa '{}': '{}'", placa, encrypted);
            }
        } catch (Exception e) {
            log.error("Falha ao criptografar placa '{}' com PostgresCryptoUtil: {}", placa, e.getMessage(), e);
        }

        log.error("ERRO CRÍTICO: Impossível criptografar placa '{}' - ABORTANDO OPERAÇÃO!", placa);
        throw new RuntimeException("Falha crítica na criptografia da placa: " + placa);
    }

    public String decryptPlaca(String placaEncrypted) {
        if (placaEncrypted == null || placaEncrypted.trim().isEmpty() || "N/A".equals(placaEncrypted)) {
            return placaEncrypted;
        }

        if (!isEncrypted(placaEncrypted)) {
            return placaEncrypted;
        }

        try {
            String decrypted = postgresCryptoUtil.decrypt(placaEncrypted);
            log.trace("Placa descriptografada com sucesso");
            return decrypted;
        } catch (Exception e) {
            log.error("Erro ao descriptografar placa: {}", e.getMessage());
            return placaEncrypted;
        }
    }

    public String encryptContrato(String contrato) {
        if (contrato == null || contrato.trim().isEmpty() || "N/A".equals(contrato)) {
            return contrato;
        }

        String contratoTrimmed = contrato.trim();

        try {
            String encrypted = postgresCryptoUtil.encrypt(contratoTrimmed);
            if (encrypted != null && encrypted.length() >= 50 && encrypted.matches("[a-fA-F0-9]+")) {
                log.debug("Contrato '{}' criptografado com PostgresCryptoUtil: {} chars", contrato, encrypted.length());
                return encrypted;
            } else {
                log.error("PostgresCryptoUtil retornou valor suspeito para contrato '{}': '{}'", contrato, encrypted);
            }
        } catch (Exception e) {
            log.error("Falha ao criptografar contrato '{}' com PostgresCryptoUtil: {}", contrato, e.getMessage(), e);
        }

        log.error("ERRO CRÍTICO: Impossível criptografar contrato '{}' - ABORTANDO OPERAÇÃO!", contrato);
        throw new RuntimeException("Falha crítica na criptografia do contrato: " + contrato);
    }

    public String decryptContrato(String contratoEncrypted) {
        if (contratoEncrypted == null || contratoEncrypted.trim().isEmpty() || "N/A".equals(contratoEncrypted)) {
            return contratoEncrypted;
        }

        if (!isEncrypted(contratoEncrypted)) {
            return contratoEncrypted;
        }

        try {
            String decrypted = postgresCryptoUtil.decrypt(contratoEncrypted);
            log.trace("Contrato descriptografado com sucesso");
            return decrypted;
        } catch (Exception e) {
            log.error("Erro ao descriptografar contrato: {}", e.getMessage());
            return contratoEncrypted;
        }
    }

    private boolean isEncrypted(String value) {
        if (value == null || value.trim().isEmpty() || "N/A".equals(value)) {
            return false;
        }

        return value.length() >= 50 && value.matches("[a-fA-F0-9]+");
    }
}