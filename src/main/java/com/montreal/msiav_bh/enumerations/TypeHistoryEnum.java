package com.montreal.msiav_bh.enumerations;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TypeHistoryEnum {

    LOCATION("Localização"),
    COLLECTED("Recolhimento"),
    IMPOUND_LOT("Pátio");

    private final String description;

}