package com.montreal.broker.dto.enumerations;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SendTypeEnum {

    EMAIL(1),
    SMS(2),
    WHATSAPP(3);

    private final Integer description;

    public static SendTypeEnum valueOf(Integer description) {
        for (SendTypeEnum value : SendTypeEnum.values()) {
            if (value.getDescription().equals(description)) {
                return value;
            }
        }
        return null;
    }

}
