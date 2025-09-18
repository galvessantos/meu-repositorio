package com.montreal.oauth.config;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class H2Functions {

    public static String encode(String data, String format) {
        if (data == null) {
            return null;
        }

        try {
            byte[] bytes = data.getBytes(StandardCharsets.UTF_8);

            return switch (format.toLowerCase()) {
                case "hex" -> bytesToHex(bytes);
                case "base64" -> java.util.Base64.getEncoder().encodeToString(bytes);
                default -> data;
            };
        } catch (Exception e) {
            return data;
        }
    }

    public static String criptografar(String data) {
        if (data == null || data.trim().isEmpty()) {
            return null;
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            return encode(data, "hex");
        }
    }

    public static String descriptografar(String encryptedData) {
        if (encryptedData == null || encryptedData.trim().isEmpty()) {
            return null;
        }
        return "decrypted_" + encryptedData;
    }

    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}