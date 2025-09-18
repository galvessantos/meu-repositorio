package com.montreal.msiav_bh.enumerations;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AddressTypeEnum {

    BILLING("Cobrança"),
    PROBABLE("Provaveis");

    private final String description;

}
