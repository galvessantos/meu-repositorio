package com.montreal.msiav_bh.enumerations;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum VehicleConditionEnum {


    OTIMO(1, "Ótimo estado de conservação"),
    BOM(2, "Bom estado de conservação"),
    REGULAR(3, "Estado de conservação regular"),
    RUIM(4, "Ruim estado de conservação");

    private final int key;
    private final String description;
}
