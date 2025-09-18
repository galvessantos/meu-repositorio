package com.montreal.core.exception_handler;

import lombok.Getter;

@Getter
public enum ProblemType {

    ERRO_DE_SISTEMA("/erro-de-sistema", "Erro de sistema"),
    DADOS_INVALIDOS("/dados-invalidos", "Dados inválidos"),
    TOKEN_INVALIDOS("/token-invalido", "Dados expirados"),
    PARAMETRO_INVALIDO("/parametro-invalido", "Parâmetro inválido"),
    MENSAGEM_INCOMPREENSIVEL("/mensagem-incompreensivel", "Mensagem incompreensível"),
    RECURSO_NAO_ENCONTRADO("/recurso-nao-encontrado", "Recurso não encontrado"),
    ENTIDADE_EM_USO("/entidade-em-uso", "Entidade em uso"),
    ERRO_NEGOCIO("/erro-negocio", "Violação de regra de negócio"),
    ERRO_SISTEMA_LATERAL("/sistema-externo", "Falha em sistema externo"),
    ACESSO_NEGADO("/acesso-negado", "Acesso negado");

    private final String title;
    private final String uri;

    ProblemType(String path, String title) {
        this.uri = "https://api.montreal.com" + path;
        this.title = title;
    }
}

