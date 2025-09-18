package com.montreal.oauth.domain.helper;

import com.montreal.oauth.domain.component.CryptoComponent;

public class GenerationEncryption {

    public static final String ENCRYPT_SECRET_KEY = "awTSZLN48GzB0LQPdc17yTauCQhafz7U";

    public static void main(String[] args) throws Exception {
        var aes = new CryptoComponent();

        String usernameEnc = aes.encryptFromString("douglasdias1", ENCRYPT_SECRET_KEY);
        String passwordEnc = aes.encryptFromString("123456", ENCRYPT_SECRET_KEY);

        System.out.println("usernameEnc: " + usernameEnc);
        System.out.println("passwordEnc: " + passwordEnc);


    }

}
