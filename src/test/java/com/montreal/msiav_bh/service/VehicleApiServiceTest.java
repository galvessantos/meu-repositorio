package com.montreal.msiav_bh.service;

import com.montreal.msiav_bh.dto.PageDTO;
import com.montreal.msiav_bh.dto.VehicleDTO;
import com.montreal.msiav_bh.mapper.VehicleInquiryMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleApiServiceTest {

    @Mock
    private ApiQueryService apiQueryService;

    @Mock
    private VehicleCacheService vehicleCacheService;

    @Mock
    private VehicleInquiryMapper vehicleInquiryMapper;

    @Mock
    private VehicleDataFetchService vehicleDataFetchService;

    @InjectMocks
    private VehicleApiService vehicleApiService;

    @Test
    void shouldAlwaysQueryDatabaseFirst() {
        LocalDate dataInicio = LocalDate.now().minusDays(30);
        LocalDate dataFim = LocalDate.now();

        VehicleDTO mockVehicle = new VehicleDTO(
                1L, "Credor Test", dataInicio, "123456", "ABC-1234",
                "Model X", "SP", "São Paulo", "12345678901",
                "PROT-123", "Em andamento", "Ativo",
                LocalDateTime.now()
        );

        Page<VehicleDTO> mockPage = new PageImpl<>(List.of(mockVehicle));
        VehicleCacheService.CacheStatus mockCacheStatus = mock(VehicleCacheService.CacheStatus.class);

        when(mockCacheStatus.getTotalRecords()).thenReturn(100L);
        when(mockCacheStatus.isValid()).thenReturn(true);
        when(vehicleCacheService.getCacheStatus()).thenReturn(mockCacheStatus);
        when(vehicleCacheService.getFromCache(any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any())).thenReturn(mockPage);

        PageDTO<VehicleDTO> result = vehicleApiService.getVehiclesWithFallback(
                dataInicio, dataFim, null, null, null, null, null, null,
                null, null, null, null, 0, 10, "protocolo", "asc"
        );

        assertNotNull(result);
        assertEquals(1, result.content().size());
        verify(vehicleCacheService, times(1)).getCacheStatus();
    }

    @Test
    void shouldTryToUpdateCacheWhenInvalid() {
        LocalDate dataInicio = LocalDate.now().minusDays(30);
        LocalDate dataFim = LocalDate.now();

        VehicleDTO mockVehicle = new VehicleDTO(
                2L, "Credor Test 2", dataInicio, "654321", "XYZ-9876",
                "Model Y", "RJ", "Rio de Janeiro", "98765432100",
                "PROT-456", "Finalizado", "Recuperado",
                LocalDateTime.now().minusHours(2)
        );

        VehicleCacheService.CacheStatus mockCacheStatus = mock(VehicleCacheService.CacheStatus.class);
        Page<VehicleDTO> mockPageWithData = new PageImpl<>(List.of(mockVehicle));

        when(mockCacheStatus.getTotalRecords()).thenReturn(50L);
        when(mockCacheStatus.isValid()).thenReturn(false);
        when(vehicleCacheService.getCacheStatus()).thenReturn(mockCacheStatus);
        when(vehicleCacheService.getFromCache(any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any())).thenReturn(mockPageWithData);


        PageDTO<VehicleDTO> result = vehicleApiService.getVehiclesWithFallback(
                dataInicio, dataFim, null, null, null, null, null, null,
                null, null, null, null, 0, 10, "protocolo", "asc"
        );

        assertNotNull(result);
        assertEquals(1, result.content().size());
        verify(vehicleCacheService, times(1)).getCacheStatus();
    }

    @Test
    void shouldFetchFromApiWhenCacheIsEmpty() {
        LocalDate dataInicio = LocalDate.now().minusDays(30);
        LocalDate dataFim = LocalDate.now();

        VehicleCacheService.CacheStatus mockCacheStatus = mock(VehicleCacheService.CacheStatus.class);
        Page<VehicleDTO> emptyPage = new PageImpl<>(List.of());

        when(mockCacheStatus.getTotalRecords()).thenReturn(0L);
        when(vehicleCacheService.getCacheStatus()).thenReturn(mockCacheStatus);
        when(vehicleCacheService.getFromCache(any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any())).thenReturn(emptyPage);
        when(vehicleDataFetchService.fetchCompleteVehicleData(any(), any())).thenReturn(List.of());

        PageDTO<VehicleDTO> result = vehicleApiService.getVehiclesWithFallback(
                dataInicio, dataFim, null, null, null, null, null, null,
                null, null, null, null, 0, 10, "protocolo", "asc"
        );

        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        verify(vehicleCacheService, times(1)).getCacheStatus();
    }

    @Test
    void shouldHandleNullCacheStatus() {
        LocalDate dataInicio = LocalDate.now().minusDays(30);
        LocalDate dataFim = LocalDate.now();

        when(vehicleCacheService.getCacheStatus()).thenReturn(null);
        when(vehicleCacheService.getFromCache(any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any())).thenReturn(new PageImpl<>(List.of()));

        PageDTO<VehicleDTO> result = vehicleApiService.getVehiclesWithFallback(
                dataInicio, dataFim, null, null, null, null, null, null,
                null, null, null, null, 0, 10, "protocolo", "asc"
        );

        assertNotNull(result);
        verify(vehicleCacheService, times(1)).getCacheStatus();
    }

    @Test
    void shouldCallApiWhenDatabaseIsEmptyAndCacheOutdated() {
        LocalDate dataInicio = LocalDate.now().minusDays(1);
        LocalDate dataFim = LocalDate.now();

        VehicleCacheService.CacheStatus mockCacheStatus = mock(VehicleCacheService.CacheStatus.class);
        Page<VehicleDTO> emptyPage = new PageImpl<>(List.of());

        when(mockCacheStatus.getTotalRecords()).thenReturn(10L);
        when(mockCacheStatus.getMinutesSinceLastSync()).thenReturn(90L);
        when(vehicleCacheService.getCacheStatus()).thenReturn(mockCacheStatus);
        when(vehicleCacheService.getFromCache(any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any())).thenReturn(emptyPage);
        when(vehicleDataFetchService.fetchCompleteVehicleData(any(), any())).thenReturn(List.of());

        PageDTO<VehicleDTO> result = vehicleApiService.getVehiclesWithFallback(
                dataInicio, dataFim, null, null, null, null, null, null,
                null, null, null, null, 0, 10, "protocolo", "asc"
        );

        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        verify(vehicleCacheService, times(1)).getCacheStatus();
        verify(vehicleDataFetchService, times(1)).fetchCompleteVehicleData(any(), any());
    }

    @Test
    void shouldHandleLocalDateTimeInUltimaMovimentacao() {
        LocalDate dataInicio = LocalDate.now().minusDays(30);
        LocalDate dataFim = LocalDate.now();
        LocalDateTime ultimaMovDateTime = LocalDateTime.of(2023, 6, 22, 15, 37, 42);

        VehicleDTO mockVehicle = new VehicleDTO(
                3L, "Credor Test 3", dataInicio, "789012", "DEF-5678",
                "Model Z", "MG", "Belo Horizonte", "11122233344",
                "PROT-789", "Localizado", "Em andamento",
                ultimaMovDateTime
        );

        Page<VehicleDTO> mockPage = new PageImpl<>(List.of(mockVehicle));
        VehicleCacheService.CacheStatus mockCacheStatus = mock(VehicleCacheService.CacheStatus.class);

        when(mockCacheStatus.getTotalRecords()).thenReturn(100L);
        when(mockCacheStatus.isValid()).thenReturn(true);
        when(vehicleCacheService.getCacheStatus()).thenReturn(mockCacheStatus);
        when(vehicleCacheService.getFromCache(any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any())).thenReturn(mockPage);

        PageDTO<VehicleDTO> result = vehicleApiService.getVehiclesWithFallback(
                dataInicio, dataFim, null, null, null, null, null, null,
                null, null, null, null, 0, 10, "protocolo", "asc"
        );

        assertNotNull(result);
        assertEquals(1, result.content().size());

        VehicleDTO resultVehicle = result.content().get(0);
        assertEquals(ultimaMovDateTime, resultVehicle.ultimaMovimentacao());

        verify(vehicleCacheService, times(1)).getCacheStatus();
    }
}