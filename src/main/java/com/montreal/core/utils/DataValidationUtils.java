package com.montreal.core.utils;

import java.util.regex.Pattern;

public class DataValidationUtils {

    // Regex patterns
    private static final String CPF_REGEX = "^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$";
    private static final String EMAIL_REGEX = "^[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";
    private static final String CNPJ_REGEX = "^\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}$";
    private static final String URL_REGEX = "^(http[s]?://)?[\\w.-]+\\.[a-zA-Z]{2,}(:\\d+)?(/.*)?$";
    private static final String PHONE_REGEX = "^\\(?\\d{2}\\)?\\s?\\d{4,5}-\\d{4}$";
    private static final String CELLPHONE_REGEX = "^\\(?\\d{2}\\)?\\s?9\\d{4}-\\d{4}$";
    private static final String INTEGER_REGEX = "^-?\\d+$";
    private static final String MONEY_REGEX = "^R\\$\\s?\\d{1,3}(\\.\\d{3})*(,\\d{2})?$";

    // CPF 
    public static boolean isValidCPF(String cpf) {
        return cpf != null && Pattern.matches(CPF_REGEX, cpf) && validateCPF(cpf);
    }

    // Email 
    public static boolean isValidEmail(String email) {
        return email != null && Pattern.matches(EMAIL_REGEX, email);
    }

    // CNPJ 
    public static boolean isValidCNPJ(String cnpj) {
        return cnpj != null && Pattern.matches(CNPJ_REGEX, cnpj) && validateCNPJ(cnpj);
    }

    // URL 
    public static boolean isValidURL(String url) {
        return url != null && Pattern.matches(URL_REGEX, url);
    }

    // Telefone
    public static boolean isValidPhone(String phone) {
        return phone != null && Pattern.matches(PHONE_REGEX, phone);
    }

    // Celular
    public static boolean isValidCellphone(String cellphone) {
        return cellphone != null && Pattern.matches(CELLPHONE_REGEX, cellphone);
    }

    // Inteiro
    public static boolean isValidInteger(String integer) {
        return integer != null && Pattern.matches(INTEGER_REGEX, integer);
    }

    // Dinheiro BRL
    public static boolean isValidMoney(String money) {
        return money != null && Pattern.matches(MONEY_REGEX, money);
    }

    // CPF
    private static boolean validateCPF(String cpf) {
        cpf = cpf.replace(".", "").replace("-", "");
        if (cpf.matches("(\\d)\\1{10}")) return false;
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += (cpf.charAt(i) - '0') * (10 - i);
        }
        int firstDigit = (sum * 10) % 11;
        if (firstDigit == 10) firstDigit = 0;

        if (firstDigit != (cpf.charAt(9) - '0')) return false;

        sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += (cpf.charAt(i) - '0') * (11 - i);
        }
        int secondDigit = (sum * 10) % 11;
        if (secondDigit == 10) secondDigit = 0;

        return secondDigit == (cpf.charAt(10) - '0');
    }

    // CNPJ 
    private static boolean validateCNPJ(String cnpj) {
        cnpj = cnpj.replace(".", "").replace("-", "").replace("/", "");
        if (cnpj.matches("(\\d)\\1{13}")) return false;

        int[] weights1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] weights2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

        int sum = 0;
        for (int i = 0; i < 12; i++) {
            sum += (cnpj.charAt(i) - '0') * weights1[i];
        }
        int firstDigit = sum % 11;
        if (firstDigit < 2) firstDigit = 0;
        else firstDigit = 11 - firstDigit;

        if (firstDigit != (cnpj.charAt(12) - '0')) return false;

        sum = 0;
        for (int i = 0; i < 13; i++) {
            sum += (cnpj.charAt(i) - '0') * weights2[i];
        }
        int secondDigit = sum % 11;
        if (secondDigit < 2) secondDigit = 0;
        else secondDigit = 11 - secondDigit;

        return secondDigit == (cnpj.charAt(13) - '0');
    }
}
