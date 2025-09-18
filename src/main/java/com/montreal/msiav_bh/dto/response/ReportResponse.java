package com.montreal.msiav_bh.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportResponse {
    
    private Long id;
    private Long seizureDateId;
    private String mandateNumber;
    private LocalDate mandateDate;
    private Long witnessId;
    private String contract;
    private String contractNumber;
    private BigDecimal debtValue;
    private String towTruck;
    private String notification;
    private String arNotification;
}
