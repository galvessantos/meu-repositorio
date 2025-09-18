package com.montreal.core.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RegisterAuditStatusEnum {

    SUCCESS("sucesso"),
    ERROR("erro");

    private final String description;

}
