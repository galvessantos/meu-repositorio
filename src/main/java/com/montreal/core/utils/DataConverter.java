package com.montreal.core.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DataConverter {

	private static final DateTimeFormatter FORMATTER_BR_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATTER_US_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final DateTimeFormatter FORMATTER_BR_DATETIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter FORMATTER_US_DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final DateTimeFormatter FORMATTER_ALT1_DATE = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter FORMATTER_ALT2_DATE = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final DateTimeFormatter FORMATTER_ALT3_DATE = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    /**
     * Converte uma data brasileira (dd/MM/yyyy) para americana (yyyy-MM-dd).
     */
    public static String converterDataParaAmericano(String dataBr) {
        return converterFormatoData(dataBr, FORMATTER_BR_DATE, FORMATTER_US_DATE);
    }

    /**
     * Converte uma data americana (yyyy-MM-dd) para brasileira (dd/MM/yyyy).
     */
    public static String converterDataParaBrasileiro(String dataUs) {
        return converterFormatoData(dataUs, FORMATTER_US_DATE, FORMATTER_BR_DATE);
    }

    /**
     * Converte uma data e hora brasileira (dd/MM/yyyy HH:mm:ss) para americana (yyyy-MM-dd HH:mm:ss).
     */
    public static String converterDataHoraParaAmericano(String dataHoraBr) {
        return converterFormatoData(dataHoraBr, FORMATTER_BR_DATETIME, FORMATTER_US_DATETIME);
    }

    /**
     * Converte uma data e hora americana (yyyy-MM-dd HH:mm:ss) para brasileira (dd/MM/yyyy HH:mm:ss).
     */
    public static String converterDataHoraParaBrasileiro(String dataHoraUs) {
        return converterFormatoData(dataHoraUs, FORMATTER_US_DATETIME, FORMATTER_BR_DATETIME);
    }

    /**
     * Converte um LocalDate para String no formato desejado.
     */
    public static String converterLocalDateParaString(LocalDate data, String formato) {
        return data.format(DateTimeFormatter.ofPattern(formato));
    }

    /**
     * Converte um LocalDateTime para String no formato desejado.
     */
    public static String converterLocalDateTimeParaString(LocalDateTime dataHora, String formato) {
        return dataHora.format(DateTimeFormatter.ofPattern(formato));
    }

    /**
     * Converte uma String para LocalDate usando múltiplos formatos suportados.
     */
    public static LocalDate converterStringParaLocalDate(String data) {
        return converterStringParaData(data, FORMATTER_BR_DATE, FORMATTER_US_DATE, FORMATTER_ALT1_DATE, FORMATTER_ALT2_DATE, FORMATTER_ALT3_DATE);
    }

    /**
     * Converte uma String para LocalDateTime usando múltiplos formatos suportados.
     */
    public static LocalDateTime converterStringParaLocalDateTime(String dataHora) {
        return converterStringParaDataHora(dataHora, FORMATTER_BR_DATETIME, FORMATTER_US_DATETIME);
    }

    /**
     * Converte um timestamp (milissegundos) para LocalDate.
     */
    public static LocalDate converterTimestampParaLocalDate(long timestamp) {
        return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * Converte um timestamp (milissegundos) para LocalDateTime.
     */
    public static LocalDateTime converterTimestampParaLocalDateTime(long timestamp) {
        return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Método auxiliar para conversão de String entre diferentes formatos de data.
     */
    private static String converterFormatoData(String data, DateTimeFormatter origem, DateTimeFormatter destino) {
        try {
            return LocalDate.parse(data, origem).format(destino);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato de data inválido: " + data, e);
        }
    }

    /**
     * Método auxiliar para conversão de String em LocalDate com múltiplos formatos suportados.
     */
    private static LocalDate converterStringParaData(String data, DateTimeFormatter... formatos) {
        for (DateTimeFormatter formato : formatos) {
            try {
                return LocalDate.parse(data, formato);
            } catch (DateTimeParseException ignored) {
            }
        }
        throw new IllegalArgumentException("Formato de data inválido: " + data);
    }

    /**
     * Método auxiliar para conversão de String em LocalDateTime com múltiplos formatos suportados.
     */
    private static LocalDateTime converterStringParaDataHora(String dataHora, DateTimeFormatter... formatos) {
        for (DateTimeFormatter formato : formatos) {
            try {
                return LocalDateTime.parse(dataHora, formato);
            } catch (DateTimeParseException ignored) {
            }
        }
        throw new IllegalArgumentException("Formato de data e hora inválido: " + dataHora);
    }
    
}
