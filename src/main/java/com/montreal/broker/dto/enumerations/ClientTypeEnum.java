package com.montreal.broker.dto.enumerations;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ClientTypeEnum {

    GUARANTEE(2);

    private final Integer description;

}
