package com.montreal.msiav_bh.service;

import com.montreal.integration.service.dbtranms.DetranMSIntegrationService;
import com.montreal.msiav_bh.dto.SeizureNoticeDTO;
import com.montreal.msiav_bh.dto.request.DetranMSSendSeizureNoticeRequest;
import com.montreal.msiav_bh.enumerations.SeizureStatusEnum;
import com.montreal.msiav_bh.enumerations.VehicleConditionEnum;
import com.montreal.msiav_bh.enumerations.VehicleStageEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DetranMSServiceTest {

    private static final String CONTRACT_NUMBER = "12345";
    private static final String TOKEN = "test-token";
    private static final String NSU = "123";
    private static final Long SEIZURE_ID = 1L;
    private static final Long VEHICLE_ID = 2L;

    @Mock
    private VehicleService vehicleService;

    @Mock
    private VehicleSeizureService vehicleSeizureService;

    @Mock
    private DetranMSIntegrationService detranMSIntegrationService;

    @InjectMocks
    private DetranMSService detranMSService;

    @Test
    @DisplayName("Should process vehicles with seizure date more than five working days")
    void shouldProcessVehiclesWithSeizureDateMoreThanFiveWorkingDays() {
        LocalDateTime seizureDateTime = LocalDateTime.now().minusDays(20);
        SeizureNoticeDTO seizureNotice = createSeizureNotice(seizureDateTime);
        List<SeizureNoticeDTO> seizureNotices = List.of(seizureNotice);

        when(vehicleSeizureService.getSeizureNoticeListByStageAndStatus(
                VehicleStageEnum.CANCELAMENTO_PEDIDO_CREDOR, SeizureStatusEnum.CONCLUIDA))
                .thenReturn(seizureNotices);
        when(detranMSIntegrationService.getToken()).thenReturn(TOKEN);

        detranMSService.process();

        verify(vehicleSeizureService).getSeizureNoticeListByStageAndStatus(
                VehicleStageEnum.CANCELAMENTO_PEDIDO_CREDOR, SeizureStatusEnum.CONCLUIDA);
        verify(detranMSIntegrationService).getToken();
        verify(detranMSIntegrationService).sendSeizureNotice(eq(TOKEN), any(DetranMSSendSeizureNoticeRequest.class));
        verify(vehicleSeizureService).updateVehicleSeizureStatus(SEIZURE_ID, SeizureStatusEnum.CONCLUIDA);
    }

    @Test
    @DisplayName("Should not process vehicles with seizure date less than five working days")
    void shouldNotProcessVehiclesWithSeizureDateLessThanFiveWorkingDays() {
        LocalDateTime seizureDateTime = LocalDateTime.now().minusDays(2);
        SeizureNoticeDTO seizureNotice = createSeizureNotice(seizureDateTime);
        List<SeizureNoticeDTO> seizureNotices = List.of(seizureNotice);

        when(vehicleSeizureService.getSeizureNoticeListByStageAndStatus(
                VehicleStageEnum.CANCELAMENTO_PEDIDO_CREDOR, SeizureStatusEnum.CONCLUIDA))
                .thenReturn(seizureNotices);

        detranMSService.process();

        verify(vehicleSeizureService).getSeizureNoticeListByStageAndStatus(
                VehicleStageEnum.CANCELAMENTO_PEDIDO_CREDOR, SeizureStatusEnum.CONCLUIDA);
        verify(detranMSIntegrationService, never()).getToken();
        verify(detranMSIntegrationService, never()).sendSeizureNotice(anyString(), any(DetranMSSendSeizureNoticeRequest.class));
        verify(vehicleSeizureService, never()).updateVehicleSeizureStatus(anyLong(), any(SeizureStatusEnum.class));
    }

    @Test
    @DisplayName("Should not process vehicles when token is blank")
    void shouldNotProcessVehiclesWhenTokenIsBlank() {
        LocalDateTime seizureDateTime = LocalDateTime.now().minusDays(20);
        SeizureNoticeDTO seizureNotice = createSeizureNotice(seizureDateTime);
        List<SeizureNoticeDTO> seizureNotices = List.of(seizureNotice);

        when(vehicleSeizureService.getSeizureNoticeListByStageAndStatus(
                VehicleStageEnum.CANCELAMENTO_PEDIDO_CREDOR, SeizureStatusEnum.CONCLUIDA))
                .thenReturn(seizureNotices);
        when(detranMSIntegrationService.getToken()).thenReturn("");

        detranMSService.process();

        verify(vehicleSeizureService).getSeizureNoticeListByStageAndStatus(
                VehicleStageEnum.CANCELAMENTO_PEDIDO_CREDOR, SeizureStatusEnum.CONCLUIDA);
        verify(detranMSIntegrationService).getToken();
        verify(detranMSIntegrationService, never()).sendSeizureNotice(anyString(), any(DetranMSSendSeizureNoticeRequest.class));
        verify(vehicleSeizureService, never()).updateVehicleSeizureStatus(anyLong(), any(SeizureStatusEnum.class));
    }

    @Test
    @DisplayName("Should handle exception during processing")
    void shouldHandleExceptionDuringProcessing() {
        when(vehicleSeizureService.getSeizureNoticeListByStageAndStatus(
                VehicleStageEnum.CANCELAMENTO_PEDIDO_CREDOR, SeizureStatusEnum.CONCLUIDA))
                .thenThrow(new RuntimeException("Test exception"));

        detranMSService.process();

        verify(vehicleSeizureService).getSeizureNoticeListByStageAndStatus(
                VehicleStageEnum.CANCELAMENTO_PEDIDO_CREDOR, SeizureStatusEnum.CONCLUIDA);
        verify(detranMSIntegrationService, never()).getToken();
        verify(detranMSIntegrationService, never()).sendSeizureNotice(anyString(), any(DetranMSSendSeizureNoticeRequest.class));
        verify(vehicleSeizureService, never()).updateVehicleSeizureStatus(anyLong(), any(SeizureStatusEnum.class));
    }

    @Test
    @DisplayName("Should process multiple vehicles")
    void shouldProcessMultipleVehicles() {
        LocalDateTime seizureDateTime1 = LocalDateTime.now().minusDays(20);
        LocalDateTime seizureDateTime2 = LocalDateTime.now().minusDays(25);

        SeizureNoticeDTO seizureNotice1 = createSeizureNotice(seizureDateTime1);
        SeizureNoticeDTO seizureNotice2 = SeizureNoticeDTO.builder()
                .contractNumber(CONTRACT_NUMBER)
                .vehicleCondition(VehicleConditionEnum.BOM)
                .seizureDateTime(seizureDateTime2)
                .seizureId(3L)
                .vehicleId(4L)
                .build();

        List<SeizureNoticeDTO> seizureNotices = List.of(seizureNotice1, seizureNotice2);

        when(vehicleSeizureService.getSeizureNoticeListByStageAndStatus(
                VehicleStageEnum.CANCELAMENTO_PEDIDO_CREDOR, SeizureStatusEnum.CONCLUIDA))
                .thenReturn(seizureNotices);
        when(detranMSIntegrationService.getToken()).thenReturn(TOKEN);

        detranMSService.process();

        verify(vehicleSeizureService).getSeizureNoticeListByStageAndStatus(
                VehicleStageEnum.CANCELAMENTO_PEDIDO_CREDOR, SeizureStatusEnum.CONCLUIDA);
        verify(detranMSIntegrationService, times(2)).getToken();
        verify(detranMSIntegrationService, times(2)).sendSeizureNotice(eq(TOKEN), any(DetranMSSendSeizureNoticeRequest.class));
        verify(vehicleSeizureService).updateVehicleSeizureStatus(SEIZURE_ID, SeizureStatusEnum.CONCLUIDA);
        verify(vehicleSeizureService).updateVehicleSeizureStatus(3L, SeizureStatusEnum.CONCLUIDA);
    }

    @Test
    @DisplayName("Should create correct DetranMSSendSeizureNoticeRequest")
    void shouldCreateCorrectDetranMSSendSeizureNoticeRequest() {
        LocalDateTime seizureDateTime = LocalDateTime.now().minusDays(20);
        SeizureNoticeDTO seizureNotice = createSeizureNotice(seizureDateTime);
        List<SeizureNoticeDTO> seizureNotices = List.of(seizureNotice);

        when(vehicleSeizureService.getSeizureNoticeListByStageAndStatus(
                VehicleStageEnum.CANCELAMENTO_PEDIDO_CREDOR, SeizureStatusEnum.CONCLUIDA))
                .thenReturn(seizureNotices);
        when(detranMSIntegrationService.getToken()).thenReturn(TOKEN);

        detranMSService.process();

        verify(detranMSIntegrationService).sendSeizureNotice(eq(TOKEN), any(DetranMSSendSeizureNoticeRequest.class));
    }

    @Test
    @DisplayName("Should handle null contract response")
    void shouldHandleNullContractResponse() {
        LocalDateTime seizureDateTime = LocalDateTime.now().minusDays(20);
        SeizureNoticeDTO seizureNotice = createSeizureNotice(seizureDateTime);
        List<SeizureNoticeDTO> seizureNotices = List.of(seizureNotice);

        when(vehicleSeizureService.getSeizureNoticeListByStageAndStatus(
                VehicleStageEnum.CANCELAMENTO_PEDIDO_CREDOR, SeizureStatusEnum.CONCLUIDA))
                .thenReturn(seizureNotices);
        when(detranMSIntegrationService.getToken()).thenReturn(TOKEN);

        detranMSService.process();

        verify(vehicleSeizureService).getSeizureNoticeListByStageAndStatus(
                VehicleStageEnum.CANCELAMENTO_PEDIDO_CREDOR, SeizureStatusEnum.CONCLUIDA);
        verify(detranMSIntegrationService).getToken();

        verify(detranMSIntegrationService).sendSeizureNotice(eq(TOKEN), any(DetranMSSendSeizureNoticeRequest.class));
        verify(vehicleSeizureService).updateVehicleSeizureStatus(SEIZURE_ID, SeizureStatusEnum.CONCLUIDA);
    }

    @Test
    @DisplayName("Should handle empty seizure notice list")
    void shouldHandleEmptySeizureNoticeList() {
        when(vehicleSeizureService.getSeizureNoticeListByStageAndStatus(
                VehicleStageEnum.CANCELAMENTO_PEDIDO_CREDOR, SeizureStatusEnum.CONCLUIDA))
                .thenReturn(List.of());

        detranMSService.process();

        verify(vehicleSeizureService).getSeizureNoticeListByStageAndStatus(
                VehicleStageEnum.CANCELAMENTO_PEDIDO_CREDOR, SeizureStatusEnum.CONCLUIDA);
        verify(detranMSIntegrationService, never()).getToken();
        verify(detranMSIntegrationService, never()).sendSeizureNotice(anyString(), any(DetranMSSendSeizureNoticeRequest.class));
        verify(vehicleSeizureService, never()).updateVehicleSeizureStatus(anyLong(), any(SeizureStatusEnum.class));
    }

    private SeizureNoticeDTO createSeizureNotice(LocalDateTime seizureDateTime) {
        return SeizureNoticeDTO.builder()
                .contractNumber(CONTRACT_NUMBER)
                .vehicleCondition(VehicleConditionEnum.OTIMO)
                .seizureDateTime(seizureDateTime)
                .seizureId(SEIZURE_ID)
                .vehicleId(VEHICLE_ID)
                .build();
    }
}
