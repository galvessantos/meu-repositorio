package com.montreal.core.domain.enumerations;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MessageTypeEnum {

    MSG_OK(200, "Requisição executada com sucesso", "OK"),
    MSG_CREATED(201, "Criado com sucesso", "CREATED"),
    MSG_NOT_FOUND(404, "Recurso não existe", "NOT_FOUND"),
    MSG_BAD_REQUEST(400, "Parâmetro inválido", "BAD_REQUEST"),
    MSG_FORBIDDEN(403, "Recurso não encontrado", "FORBIDDEN"),
    MSG_UNAUTHORIZED(401, "Acesso negado", "UNAUTHORIZED");

    private final Integer status;
    private final String title;
    private final String type;

}
