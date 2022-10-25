package com.kyc.users.service;

import com.kyc.core.properties.KycMessages;
import com.kyc.users.entity.KycLoginHistoric;
import com.kyc.users.entity.KycLoginUserInfo;
import com.kyc.users.entity.KycParameter;
import com.kyc.users.entity.KycUser;
import com.kyc.users.model.SessionData;
import com.kyc.users.repositories.KycUserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Optional;

import static com.kyc.users.constants.AppConstants.KYC_SESSION_TIMEOUT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SessionServiceTest {

    @Mock
    private HistoricLoginService historicLoginService;

    @Mock
    private ParameterService parameterService;

    @Mock
    private KycUserRepository kycUserRepository;

    @Mock
    private KycMessages kycMessages;

    @Mock
    private Clock clock;

    @InjectMocks
    private SessionService sessionService;

    private static final ZonedDateTime NOW = ZonedDateTime.of(
            2022,
            9,
            10,
            10,
            10,
            10,
            0,
            ZoneId.of("UTC-06")
    );

    @BeforeAll
    public static void init(){
        MockitoAnnotations.openMocks(SessionServiceTest.class);
    }

    @Test
    public void openSession_firstTimeOpeningSession_openedSession(){

        KycUser user = new KycUser();

        SessionData sessionData = SessionData.builder()
                .kycUser(user)
                .sessionId("id")
                .build();

        sessionService.openSession(sessionData);
        verify(historicLoginService,times(1)).addHistoricLoginData(any(SessionData.class));
    }

    @Test
    public void openSession_openNewSession_openedSession(){

        KycUser user = new KycUser();
        user.setLoginUserInfo(new KycLoginUserInfo());

        SessionData sessionData = SessionData.builder()
                .kycUser(user)
                .sessionId("id")
                .build();

        sessionService.openSession(sessionData);
        verify(historicLoginService,times(1)).addHistoricLoginData(any(SessionData.class));
    }

    @Test
    public void closeSession_closingActiveSession_closedSession(){

        KycUser user = new KycUser();

        SessionData sessionData = SessionData.builder()
                .kycUser(user)
                .sessionId("id")
                .build();

        KycLoginHistoric kycLoginHistoric = new KycLoginHistoric();
        kycLoginHistoric.setDateCheckpoint(Date.from(NOW.minusMinutes(3L).toInstant()));

        when(parameterService.getParameter(KYC_SESSION_TIMEOUT))
                .thenReturn(new KycParameter("kyc","3"));
        when(historicLoginService.getCurrentSession(any(SessionData.class)))
                .thenReturn(Optional.of(kycLoginHistoric));
        when(clock.instant()).thenReturn(NOW.toInstant());
        when(clock.getZone()).thenReturn(NOW.getZone());

        sessionService.closeSession(sessionData);
        verify(historicLoginService,times(1)).addHistoricLogoutData(any(SessionData.class));

    }

    @Test
    public void closeSession_closingActiveSession_closedSession2(){

        KycUser user = new KycUser();

        SessionData sessionData = SessionData.builder()
                .kycUser(user)
                .sessionId("id")
                .build();

        KycLoginHistoric kycLoginHistoric = new KycLoginHistoric();
        kycLoginHistoric.setDateCheckpoint(Date.from(NOW.minusMinutes(1L).toInstant()));

        when(parameterService.getParameter(KYC_SESSION_TIMEOUT))
                .thenReturn(new KycParameter("kyc","3"));
        when(historicLoginService.getCurrentSession(any(SessionData.class)))
                .thenReturn(Optional.of(kycLoginHistoric));
        when(clock.instant()).thenReturn(NOW.toInstant());
        when(clock.getZone()).thenReturn(NOW.getZone());

        sessionService.closeSession(sessionData);
        verify(historicLoginService,times(1)).addHistoricLogoutData(any(SessionData.class));

    }

    @Test
    public void closeSession_closingInactiveSession_nothingToDo(){

        KycUser user = new KycUser();

        SessionData sessionData = SessionData.builder()
                .kycUser(user)
                .sessionId("id")
                .build();

        KycLoginHistoric kycLoginHistoric = new KycLoginHistoric();
        kycLoginHistoric.setDateCheckpoint(Date.from(NOW.minusMinutes(4L).toInstant()));

        when(parameterService.getParameter(KYC_SESSION_TIMEOUT))
                .thenReturn(new KycParameter("kyc","3"));
        when(historicLoginService.getCurrentSession(any(SessionData.class)))
                .thenReturn(Optional.of(kycLoginHistoric));
        when(clock.instant()).thenReturn(NOW.toInstant());
        when(clock.getZone()).thenReturn(NOW.getZone());

        sessionService.closeSession(sessionData);
        verify(historicLoginService,times(0)).addHistoricLogoutData(any(SessionData.class));

    }

    @Test
    public void closeIdleActiveSessions_closingIdleActiveSessions_idleSessionsWereClosed(){


        KycLoginHistoric kycLoginHistoric = new KycLoginHistoric();
        kycLoginHistoric.setDateCheckpoint(Date.from(NOW.minusMinutes(14L).toInstant()));

        when(historicLoginService.getActiveSessions()).thenReturn(Collections.singletonList(kycLoginHistoric));
        when(parameterService.getParameter(KYC_SESSION_TIMEOUT)).thenReturn(new KycParameter("kyc","3"));
        when(clock.instant()).thenReturn(NOW.toInstant());
        when(clock.getZone()).thenReturn(NOW.getZone());

        sessionService.closeIdleActiveSessions();
        verify(historicLoginService,times(1)).addHistoricLogoutData(any(SessionData.class));
    }

    @Test
    public void closeIdleActiveSessions_checkingActiveSessionButNoIdle_sessionWasNotClosed(){


        KycLoginHistoric kycLoginHistoric = new KycLoginHistoric();
        kycLoginHistoric.setDateCheckpoint(Date.from(NOW.minusMinutes(1L).toInstant()));

        when(historicLoginService.getActiveSessions()).thenReturn(Collections.singletonList(kycLoginHistoric));
        when(parameterService.getParameter(KYC_SESSION_TIMEOUT)).thenReturn(new KycParameter("kyc","3"));
        when(clock.instant()).thenReturn(NOW.toInstant());
        when(clock.getZone()).thenReturn(NOW.getZone());

        sessionService.closeIdleActiveSessions();
        verify(historicLoginService,times(0)).addHistoricLogoutData(any(SessionData.class));
    }

    @Test
    public void closeIdleActiveSessions_noIdleSessions_noneSessionWasClosed(){

        when(historicLoginService.getActiveSessions()).thenReturn(Collections.emptyList());

        sessionService.closeIdleActiveSessions();
        verify(historicLoginService,times(0)).addHistoricLogoutData(any(SessionData.class));
    }

    @Test
    public void renewSession_renewingSession_sessionWasRenewed(){

        KycUser user = new KycUser();

        SessionData sessionData = SessionData.builder()
                .kycUser(user)
                .sessionId("id")
                .build();

        KycLoginHistoric kycLoginHistoric = new KycLoginHistoric();
        kycLoginHistoric.setDateCheckpoint(Date.from(NOW.minusMinutes(1L).toInstant()));

        when(parameterService.getParameter(KYC_SESSION_TIMEOUT)).thenReturn(new KycParameter("kyc","3"));
        when(historicLoginService.getCurrentSession(any(SessionData.class))).thenReturn(Optional.of(kycLoginHistoric));
        when(clock.instant()).thenReturn(NOW.toInstant());
        when(clock.getZone()).thenReturn(NOW.getZone());

        sessionService.renewSession(sessionData);
        verify(historicLoginService,times(1)).refreshCheckpoint(any(SessionData.class));
    }

    @Test
    public void renewSession_sessionNoActive_sessionWasNotRenewed(){

        KycUser user = new KycUser();

        SessionData sessionData = SessionData.builder()
                .kycUser(user)
                .sessionId("id")
                .build();

        when(historicLoginService.getCurrentSession(any(SessionData.class))).thenReturn(Optional.empty());

        sessionService.renewSession(sessionData);
        verify(historicLoginService,times(0)).refreshCheckpoint(any(SessionData.class));
    }

    @Test
    public void hasActiveSession_theSessionIsActive_returnTrue(){

        KycUser user = new KycUser();

        SessionData sessionData = SessionData.builder()
                .kycUser(user)
                .sessionId("id")
                .build();

        KycLoginHistoric kycLoginHistoric = new KycLoginHistoric();
        kycLoginHistoric.setDateCheckpoint(Date.from(NOW.minusMinutes(1L).toInstant()));

        when(parameterService.getParameter(KYC_SESSION_TIMEOUT)).thenReturn(new KycParameter("kyc","3"));
        when(historicLoginService.getCurrentSession(any(SessionData.class))).thenReturn(Optional.of(kycLoginHistoric));
        when(clock.instant()).thenReturn(NOW.toInstant());
        when(clock.getZone()).thenReturn(NOW.getZone());

        Assertions.assertTrue(sessionService.hasActiveSession(sessionData));
    }

    @Test
    public void hasActiveSession_theSessionIsNotActive_returnFalse(){

        KycUser user = new KycUser();

        SessionData sessionData = SessionData.builder()
                .kycUser(user)
                .sessionId("id")
                .build();

        when(historicLoginService.getCurrentSession(any(SessionData.class))).thenReturn(Optional.empty());

        Assertions.assertFalse(sessionService.hasActiveSession(sessionData));

    }

    @Test
    public void hasActiveSessionOnChannel_ASessionIsActiveOnChannel_returnTrue(){

        KycLoginHistoric kycLoginHistoric = new KycLoginHistoric();
        kycLoginHistoric.setDateCheckpoint(Date.from(NOW.minusMinutes(1L).toInstant()));

        when(parameterService.getParameter(KYC_SESSION_TIMEOUT)).thenReturn(new KycParameter("kyc","3"));
        when(historicLoginService.getCurrentSessionOnChannel(1L,1)).thenReturn(Optional.of(kycLoginHistoric));
        when(clock.instant()).thenReturn(NOW.toInstant());
        when(clock.getZone()).thenReturn(NOW.getZone());

        Assertions.assertTrue(sessionService.hasActiveSessionOnChannel(1L,1));
    }

    @Test
    public void hasActiveSessionOnChannel_NoSessionActiveOnTheChannel_returnFalse(){

        when(historicLoginService.getCurrentSessionOnChannel(1L,1)).thenReturn(Optional.empty());

        Assertions.assertFalse(sessionService.hasActiveSessionOnChannel(1L,1));
    }


}
