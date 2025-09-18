package com.montreal.core.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostgresCryptoUtil {

    private final JdbcTemplate jdbcTemplate;

    public String encrypt(String plainText) {
        try {
        	if(plainText != null && !"".equalsIgnoreCase(plainText)) {
	            String sql = "SELECT encode(criptografar(?::text), 'hex')";
	            return jdbcTemplate.queryForObject(sql, String.class, plainText);
        	}
        	return plainText;
        } catch (Exception e) {
            log.error("Erro ao criptografar valor '{}': {}", plainText, e.getMessage(), e);
            throw new RuntimeException("Erro ao criptografar valor", e);
        }
    }

    public String decrypt(String encryptedHex) {
        try {
        	if(encryptedHex != null && !"".equalsIgnoreCase(encryptedHex) && encryptedHex.length() >= 50) {
        		String sql = "SELECT descriptografar(decode(?::text, 'hex'))";
        		return jdbcTemplate.queryForObject(sql, String.class, encryptedHex);
        	}
        	return null;
        } catch (Exception e) {
            log.error("Erro ao descriptografar valor '{}': {}", encryptedHex, e.getMessage(), e);
            throw new RuntimeException("Erro ao descriptografar valor", e);
        }
    }
}
