package com.montreal.integration.service.dbtranms;

import com.montreal.msiav_bh.client.DetranMSClient;
import com.montreal.msiav_bh.dto.request.DetranMSSendSeizureNoticeRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DetranMSSendSeizureNoticeService {
    private final DetranMSClient detranMSClient;

    protected void send(String authorization, DetranMSSendSeizureNoticeRequest request) {
        try {
            detranMSClient.sendSeizureNotice(authorization, request);
        } catch (Exception e) {
            log.error("Failed to send seizure notice Detran MS. Authorization: {} NSU: {}. Error: {}", authorization, request.nsu(), e.getMessage());
        }
    }
}
