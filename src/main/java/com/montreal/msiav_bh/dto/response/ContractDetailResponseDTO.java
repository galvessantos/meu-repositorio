package com.montreal.msiav_bh.dto.response;

import com.montreal.msiav_bh.dto.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ContractDetailResponseDTO {
    private ContractDTO consulta;
    private List<VehicleDTO> veiculos;
    private List<DebtorDTO> devedores;
    private CreditorDTO credor;
    private NotaryDTO serventia;
    private DetranDTO detran;
    private List<AgencyDTO> mandatarios = new ArrayList<>();;

}

