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

    public String encryptCpfDevedor(String cpfDevedor) {
        if (cpfDevedor == null || cpfDevedor.trim().isEmpty() || "N/A".equals(cpfDevedor)) {
            return cpfDevedor;
        }

        String cpfTrimmed = cpfDevedor.trim();

        try {
            String encrypted = postgresCryptoUtil.encrypt(cpfTrimmed);
            if (encrypted != null && encrypted.length() >= 50 && encrypted.matches("[a-fA-F0-9]+")) {
                log.debug("CPF '{}' criptografado com PostgresCryptoUtil: {} chars", cpfDevedor, encrypted.length());
                return encrypted;
            } else {
                log.error("PostgresCryptoUtil retornou valor suspeito para CPF '{}': '{}'", cpfDevedor, encrypted);
            }
        } catch (Exception e) {
            log.error("Falha ao criptografar CPF '{}' com PostgresCryptoUtil: {}", cpfDevedor, e.getMessage(), e);
        }

        log.error("ERRO CRÍTICO: Impossível criptografar CPF '{}' - ABORTANDO OPERAÇÃO!", cpfDevedor);
        throw new RuntimeException("Falha crítica na criptografia do CPF: " + cpfDevedor);
    }

    public String decryptCpfDevedor(String cpfEncrypted) {
        if (cpfEncrypted == null || cpfEncrypted.trim().isEmpty() || "N/A".equals(cpfEncrypted)) {
            return cpfEncrypted;
        }

        if (!isEncrypted(cpfEncrypted)) {
            return cpfEncrypted;
        }

        try {
            String decrypted = postgresCryptoUtil.decrypt(cpfEncrypted);
            log.trace("CPF descriptografado com sucesso");
            return decrypted;
        } catch (Exception e) {
            log.error("Erro ao descriptografar CPF: {}", e.getMessage());
            return cpfEncrypted;
        }
    }

    public String encryptProtocolo(String protocolo) {
        if (protocolo == null || protocolo.trim().isEmpty() || "N/A".equals(protocolo)) {
            return protocolo;
        }

        String protocoloTrimmed = protocolo.trim();

        try {
            String encrypted = postgresCryptoUtil.encrypt(protocoloTrimmed);
            if (encrypted != null && encrypted.length() >= 50 && encrypted.matches("[a-fA-F0-9]+")) {
                log.debug("Protocolo '{}' criptografado com PostgresCryptoUtil: {} chars", protocolo, encrypted.length());
                return encrypted;
            } else {
                log.error("PostgresCryptoUtil retornou valor suspeito para protocolo '{}': '{}'", protocolo, encrypted);
            }
        } catch (Exception e) {
            log.error("Falha ao criptografar protocolo '{}' com PostgresCryptoUtil: {}", protocolo, e.getMessage(), e);
        }

        log.error("ERRO CRÍTICO: Impossível criptografar protocolo '{}' - ABORTANDO OPERAÇÃO!", protocolo);
        throw new RuntimeException("Falha crítica na criptografia do protocolo: " + protocolo);
    }

    public String decryptProtocolo(String protocoloEncrypted) {
        if (protocoloEncrypted == null || protocoloEncrypted.trim().isEmpty() || "N/A".equals(protocoloEncrypted)) {
            return protocoloEncrypted;
        }

        if (!isEncrypted(protocoloEncrypted)) {
            return protocoloEncrypted;
        }

        try {
            String decrypted = postgresCryptoUtil.decrypt(protocoloEncrypted);
            log.trace("Protocolo descriptografado com sucesso");
            return decrypted;
        } catch (Exception e) {
            log.error("Erro ao descriptografar protocolo: {}", e.getMessage());
            return protocoloEncrypted;
        }
    }

    public String encryptCidade(String cidade) {
        if (cidade == null || cidade.trim().isEmpty() || "N/A".equals(cidade)) {
            return cidade;
        }

        String cidadeTrimmed = cidade.trim();

        try {
            String encrypted = postgresCryptoUtil.encrypt(cidadeTrimmed);
            if (encrypted != null && encrypted.length() >= 50 && encrypted.matches("[a-fA-F0-9]+")) {
                log.debug("Cidade '{}' criptografada com PostgresCryptoUtil: {} chars", cidade, encrypted.length());
                return encrypted;
            } else {
                log.error("PostgresCryptoUtil retornou valor suspeito para cidade '{}': '{}'", cidade, encrypted);
            }
        } catch (Exception e) {
            log.error("Falha ao criptografar cidade '{}' com PostgresCryptoUtil: {}", cidade, e.getMessage(), e);
        }

        log.error("ERRO CRÍTICO: Impossível criptografar cidade '{}' - ABORTANDO OPERAÇÃO!", cidade);
        throw new RuntimeException("Falha crítica na criptografia da cidade: " + cidade);
    }

    public String decryptCidade(String cidadeEncrypted) {
        if (cidadeEncrypted == null || cidadeEncrypted.trim().isEmpty() || "N/A".equals(cidadeEncrypted)) {
            return cidadeEncrypted;
        }

        if (!isEncrypted(cidadeEncrypted)) {
            return cidadeEncrypted;
        }

        try {
            String decrypted = postgresCryptoUtil.decrypt(cidadeEncrypted);
            log.trace("Cidade descriptografada com sucesso");
            return decrypted;
        } catch (Exception e) {
            log.error("Erro ao descriptografar cidade: {}", e.getMessage());
            return cidadeEncrypted;
        }
    }

    private boolean isEncrypted(String value) {
        if (value == null || value.trim().isEmpty() || "N/A".equals(value)) {
            return false;
        }

        return value.length() >= 50 && value.matches("[a-fA-F0-9]+");
    }
}