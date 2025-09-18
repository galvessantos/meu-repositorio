package com.montreal.msiav_bh.dto;

import lombok.Data;

@Data
public class VehicleDataDTO {
    private String chassi;
    private String renavam;
    private Long gravame;
    private String placa;
    private String marca_modelo;
    private String cor;
    private String registro_detran;
    private Boolean possui_gps;
    private String uf_emplacamento;
    private String ano_fabricacao;
    private String ano_modelo;
}
