package com.montreal.msiav_bh.job;

import com.montreal.core.properties.DetranMSJobProperties;
import com.montreal.msiav_bh.service.DetranMSService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DetranMSSendSeizureNoticeJobTest {

    @Mock
    private DetranMSService detranMSService;

    @Mock
    private DetranMSJobProperties detranMSJobProperties;

    @InjectMocks
    private DetranMSSendSeizureNoticeJob detranMSSendSeizureNoticeJob;

    @Test
    @DisplayName("Deve não executar o job quando estiver desabilitado")
    void shouldNotExecuteJobWhenDisabled() {
        when(detranMSJobProperties.getEnabled()).thenReturn(false);

        detranMSSendSeizureNoticeJob.execute();

        verify(detranMSService, never()).process();
    }

    @Test
    @DisplayName("Deve executar o job quando estiver habilitado")
    void shouldExecuteJobWhenEnabled() {
        when(detranMSJobProperties.getEnabled()).thenReturn(true);

        detranMSSendSeizureNoticeJob.execute();

        verify(detranMSService).process();
    }
}