package com.montreal.msiav_bh.job;

import com.montreal.core.properties.DetranMSJobProperties;
import com.montreal.msiav_bh.service.DetranMSService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class DetranMSSendSeizureNoticeJob {
    private final DetranMSService detranMSService;
    private final DetranMSJobProperties detranMSJobProperties;

    @Scheduled(cron = "${detran.ms.job.cron}", zone = "America/Fortaleza")
    public void execute() {
        if (Boolean.FALSE.equals(detranMSJobProperties.getEnabled())) {
            log.info("Detran MS Send Seizure Notice Job is disabled");
            return;
        }

        long startTime = System.currentTimeMillis();
        String startDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(startTime));
        log.info("Starting Detran MS Send Seizure Notice Job at {}", startDate);
        try {
            detranMSService.process();
        } finally {
            double executionTime = (System.currentTimeMillis() - startTime) / 1000.0;
            log.info("Finished Detran MS Send Seizure Notice Job. Tempo de execução: {} s", executionTime);
        }
    }
}
