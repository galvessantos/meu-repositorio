package com.montreal.msiav_bh.utils.exceptions;

public class ValidationMessages {
    public static final String DATA_FIM_ANTERIOR_INICIO = "dataFim não pode ser anterior a dataInicio";
    public static final String DATA_INICIO_OBRIGATORIA = "Se fornecer dataFim, deve fornecer dataInicio";
    public static final String DATA_FIM_OBRIGATORIA = "Se fornecer dataInicio, deve fornecer dataFim";
    public static final String CPF_INVALIDO = "CPF/CNPJ deve ter 11 ou 14 dígitos";
    public static final String CREDOR_VAZIO = "Credor não pode ser vazio";
    public static final String UF_VAZIA = "UF não pode ser vazia";
    public static final String COMBINACAO_INVALIDA = "Quando usar credor, UF, modelo ou etapa, deve incluir também: dataInicio/dataFim, contrato ou placa";
    public static final String BUSCA_DIRETA_INVALIDA = "Busca direta por data, contrato ou placa não é permitida";
    public static final String CONTRATO_PLACA_SOZINHO = "contrato ou placa não podem ser usados sozinhos";
    public static final String FILTRO_SECUNDARIO_SEM_PRINCIPAL = "Ao usar credor, UF, modelo ou etapa, deve incluir um filtro diferente (data, contrato ou placa)";
    public static final String PERIODO_OBRIGATORIO = "Período (dataInicio e dataFim) é obrigatório para esta consulta";
    public static final String FILTRO_COMBINACAO_INVALIDA = "Combinação de filtros inválida. Consulte a documentação para as combinações permitidas";
}