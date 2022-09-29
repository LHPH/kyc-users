package com.kyc.users.service;

import com.kyc.users.entity.KycLoginHistoric;
import com.kyc.users.entity.KycUser;
import com.kyc.users.model.SessionData;
import com.kyc.users.repositories.KycLoginHistoricRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HistoricLoginServiceTest {

    @Mock
    private KycLoginHistoricRepository kycLoginHistoricRepository;

    @InjectMocks
    private HistoricLoginService historicLoginService;

    @BeforeAll
    public static void setUp(){
        MockitoAnnotations.openMocks(HistoricLoginServiceTest.class);
    }

    @Test
    public void addHistoricLoginData_savingSession_savedSession(){

        SessionData sessionData = SessionData.builder()
                .sessionId("id")
                .kycUser(new KycUser())
                .build();

        historicLoginService.addHistoricLoginData(sessionData);
        verify(kycLoginHistoricRepository,times(1))
                .save(any(KycLoginHistoric.class));
    }

    @Test
    public void addHistoricLogoutData_closingSession_closedSession(){

        SessionData sessionData = SessionData.builder()
                .sessionId("id")
                .kycUser(new KycUser())
                .build();

        KycLoginHistoric savedSession = new KycLoginHistoric();
        savedSession.setUser(new KycUser());
        savedSession.setIdSession("id");

        when(kycLoginHistoricRepository.getCurrentSession(anyString()))
                .thenReturn(Optional.of(savedSession));

        historicLoginService.addHistoricLogoutData(sessionData);
        verify(kycLoginHistoricRepository,times(1))
                .save(any(KycLoginHistoric.class));
    }

    @Test
    public void addHistoricLogoutData_nonExistingSession_nothingToDo(){

        SessionData sessionData = SessionData.builder()
                .sessionId("id")
                .kycUser(new KycUser())
                .build();

        when(kycLoginHistoricRepository.getCurrentSession(anyString()))
                .thenReturn(Optional.empty());

        historicLoginService.addHistoricLogoutData(sessionData);
        verify(kycLoginHistoricRepository,times(0))
                .save(any(KycLoginHistoric.class));
    }

    @Test
    public void refreshCheckpoint_refreshExistingSession_refreshedSession(){

        SessionData sessionData = SessionData.builder().sessionId("id").build();
        KycLoginHistoric savedSession = new KycLoginHistoric();
        savedSession.setUser(new KycUser());

        when(historicLoginService.getCurrentSession(sessionData))
                .thenReturn(Optional.of(savedSession));

        historicLoginService.refreshCheckpoint(sessionData);
        verify(kycLoginHistoricRepository,times(1)).save(any(KycLoginHistoric.class));
    }

    @Test
    public void refreshCheckpoint_refreshNonExistingSession_nothingToDo(){

        SessionData sessionData = SessionData.builder().sessionId("id").build();
        KycLoginHistoric savedSession = new KycLoginHistoric();

        when(historicLoginService.getCurrentSession(sessionData))
                .thenReturn(Optional.empty());

        historicLoginService.refreshCheckpoint(sessionData);
        verify(kycLoginHistoricRepository,times(0)).save(any(KycLoginHistoric.class));
    }

    @Test
    public void getCurrentSession_getExistingSession_returnCurrentSession(){

        SessionData sessionData = SessionData.builder().sessionId("id").build();
        historicLoginService.getCurrentSession(sessionData);
        verify(kycLoginHistoricRepository,times(1)).getCurrentSession("id");
    }

    @Test
    public void getCurrentSessionOnChannel_getExistingSession_returnCurrentSession(){

        historicLoginService.getCurrentSessionOnChannel(1L,1);
        verify(kycLoginHistoricRepository,times(1)).getCurrentSessionOnChannel(1L,1);
    }

    @Test
    public void getActiveSessions_retrieveResults_returnResults(){

        historicLoginService.getActiveSessions();
        verify(kycLoginHistoricRepository,times(1)).getActiveSessions();
    }


}
