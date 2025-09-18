package com.montreal.msiav_bh.dto.response;

import com.montreal.msiav_bh.entity.ProbableAddress;
import com.montreal.msiav_bh.entity.QueryResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContractDetails {
    private QueryDetailResponseDTO dadosApi;
}
