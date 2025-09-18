package com.montreal.msiav_bh.service;

import com.montreal.integration.service.dbtranms.DetranMSIntegrationService;
import com.montreal.msiav_bh.dto.FileDownloadDTO;
import com.montreal.msiav_bh.dto.SeizureNoticeDTO;
import com.montreal.msiav_bh.dto.request.DetranMSSendSeizureNoticeRequest;
import com.montreal.msiav_bh.enumerations.SeizureStatusEnum;
import com.montreal.msiav_bh.enumerations.VehicleConditionEnum;
import com.montreal.msiav_bh.enumerations.VehicleStageEnum;
import com.montreal.msiav_bh.utils.WorkingDayUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.montreal.msiav_bh.utils.FileUtils.resourceToBase64;

@Slf4j
@Service
@RequiredArgsConstructor
public class DetranMSService {
    private static final String ISO_8601 = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    private final VehicleService vehicleService;
    private final VehicleSeizureService vehicleSeizureService;
    private final DetranMSIntegrationService detranMSIntegrationService;

    public void process() {
        try {
            List<SeizureNoticeDTO> vehiclesToProcess = vehicleSeizureService.getSeizureNoticeListByStageAndStatus(VehicleStageEnum.CANCELAMENTO_PEDIDO_CREDOR, SeizureStatusEnum.CONCLUIDA);
            vehiclesToProcess.stream()
                    .filter(this::isSeizureDateMoreThanFiveWorkingDays)
                    .forEach(this::sendSeizureNotice);
        } catch (Exception e) {
            log.error("Error processing vehicles for Detran MS seizure notice: {}", e.getMessage(), e);
        }
    }

    private boolean isSeizureDateMoreThanFiveWorkingDays(SeizureNoticeDTO seizureNotice) {
        LocalDate vehicleSeizureDate = seizureNotice.seizureDateTime().toLocalDate();
        LocalDate now = LocalDate.now();

        int workingDays = 0;

        while (vehicleSeizureDate.isBefore(now) && workingDays <= 5) {
            vehicleSeizureDate = vehicleSeizureDate.plusDays(1);
            if (WorkingDayUtils.isWorkingDay(vehicleSeizureDate)) {
                workingDays++;
            }
        }

        return workingDays > 5;
    }

    private void sendSeizureNotice(SeizureNoticeDTO seizureNotice) {
        if (isContractCanceled(seizureNotice)) {
            log.error("Contract is canceled for vehicle with ID: {}", seizureNotice.vehicleId());
            return;
        }

        String token = detranMSIntegrationService.getToken();

        if (StringUtils.isBlank(token)) {
            return;
        }

        DetranMSSendSeizureNoticeRequest request = getDetranMsSendSeizureNoticeRequest(seizureNotice);
        detranMSIntegrationService.sendSeizureNotice(token, request);
        vehicleSeizureService.updateVehicleSeizureStatus(seizureNotice.seizureId(), SeizureStatusEnum.CONCLUIDA);
    }

    private DetranMSSendSeizureNoticeRequest getDetranMsSendSeizureNoticeRequest(SeizureNoticeDTO seizureNotice) {
        int nsu = seizureNotice.seizureId().intValue();

        VehicleConditionEnum vehicleConditionEnum = seizureNotice.vehicleCondition();

        String seizureDateFormatted = seizureNotice.seizureDateTime()
                .atZone(ZoneId.systemDefault())
                .withZoneSameInstant(ZoneId.of("UTC"))
                .format(DateTimeFormatter.ofPattern(ISO_8601));

        FileDownloadDTO pdfBySeizureVehicleId = getFileDownloadDTO();
        Resource fileResource = pdfBySeizureVehicleId.file();
        String fileBase64 = resourceToBase64(fileResource);

        return DetranMSSendSeizureNoticeRequest
                .builder()
                .nsu(nsu)
                .vehicleCondition(vehicleConditionEnum.getKey())
                .seizureDate(seizureDateFormatted)
                .file(fileBase64)
                .build();
    }

    private boolean isContractCanceled(SeizureNoticeDTO seizureNotice) {
        return seizureNotice.contractNumber() == null;
    }

    @SneakyThrows
    public FileDownloadDTO getFileDownloadDTO() {
        File file = File.createTempFile("esteira_", ".pdf");
        Resource resource = new FileSystemResource(file);

        FileDownloadDTO fileDownloadDTO = FileDownloadDTO.builder()
                .fileName(resource.getFilename())
                .fileSize(resource.contentLength())
                .file(resource)
                .build();

        file.deleteOnExit();
        return fileDownloadDTO;
    }
}
