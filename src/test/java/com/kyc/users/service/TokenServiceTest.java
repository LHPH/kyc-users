package com.kyc.users.service;

import com.kyc.core.exception.KycRestException;
import com.kyc.core.model.jwt.JWTData;
import com.kyc.core.model.web.MessageData;
import com.kyc.core.properties.KycMessages;
import com.kyc.users.entity.KycParameter;
import com.nimbusds.jose.JOSEException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;

import java.text.ParseException;

import static com.kyc.users.constants.AppConstants.KYC_SHARED_KEY;
import static com.kyc.users.constants.AppConstants.MSG_APP_002;
import static com.kyc.users.constants.AppConstants.MSG_APP_010;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TokenServiceTest {

    @Mock
    private ParameterService parameterService;

    @Mock
    private KycMessages kycMessages;

    @InjectMocks
    private TokenService tokenService;

    @BeforeAll
    public static void setUp(){

        MockitoAnnotations.openMocks(TokenServiceTest.class);
    }

    @Test
    public void getToken_generatingToken_returnToken(){

        JWTData jwtData = new JWTData();
        jwtData.setSubject("sub");

        when(parameterService.getParameter(KYC_SHARED_KEY))
                .thenReturn(new KycParameter("key","12345678901234567890123456789012"));

        String token = tokenService.getToken(jwtData);
        Assertions.assertNotNull(token);
    }

    @Test
    public void getToken_badRetrievedKey_throwException(){

        KycRestException ex = Assertions.assertThrows(KycRestException.class,()->{

            JWTData jwtData = new JWTData();
            jwtData.setSubject("sub");

            when(parameterService.getParameter(KYC_SHARED_KEY))
                    .thenReturn(new KycParameter("","bad"));
            when(kycMessages.getMessage(MSG_APP_010))
                    .thenReturn(new MessageData());
            tokenService.getToken(jwtData);
        });
        Assertions.assertTrue(ex.getException() instanceof JOSEException);
    }

    @Test
    public void getToken_unavailableDatabase_throwException(){

        KycRestException ex = Assertions.assertThrows(KycRestException.class,()->{

            JWTData jwtData = new JWTData();
            jwtData.setSubject("sub");

            when(parameterService.getParameter(KYC_SHARED_KEY))
                    .thenThrow(new InvalidDataAccessResourceUsageException("test error db"));
            when(kycMessages.getMessage(MSG_APP_010))
                    .thenReturn(new MessageData());
            tokenService.getToken(jwtData);
        });
        Assertions.assertTrue(ex.getException() instanceof DataAccessException);
    }

    @Test
    public void readToken_retrievingDataFromToken_returnData(){

        JWTData jwtData = new JWTData();
        jwtData.setSubject("sub");
        jwtData.setChannel("channel");
        jwtData.setKey("key");
        jwtData.setAudience("aud");

        when(parameterService.getParameter(KYC_SHARED_KEY))
                .thenReturn(new KycParameter("key","12345678901234567890123456789012"));

        String token = tokenService.getToken(jwtData);

        JWTData result = tokenService.readToken(token);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(jwtData.getKey(),result.getKey());
    }

    @Test
    public void readToken_badSharedKey_throwException(){

        KycRestException ex = Assertions.assertThrows(KycRestException.class,()->{

            when(parameterService.getParameter(KYC_SHARED_KEY))
                    .thenReturn(new KycParameter("key","badKey"));
            when(kycMessages.getMessage(MSG_APP_002))
                    .thenReturn(new MessageData());

            tokenService.readToken("someToken");

        });
        Assertions.assertTrue(ex.getException() instanceof ParseException);
    }

    @Test
    public void readToken_badProvidedToken_throwException(){

        KycRestException ex = Assertions.assertThrows(KycRestException.class,()->{

            when(parameterService.getParameter(KYC_SHARED_KEY))
                    .thenReturn(new KycParameter("key","12345678901234567890123456789012"));
            when(kycMessages.getMessage(MSG_APP_002))
                    .thenReturn(new MessageData());

            tokenService.readToken("badToken");

        });
        Assertions.assertTrue(ex.getException() instanceof ParseException);
    }

    @Test
    public void readToken_unavailableDatabase_throwException(){

        KycRestException ex = Assertions.assertThrows(KycRestException.class,()->{

            when(parameterService.getParameter(KYC_SHARED_KEY))
                    .thenThrow(new InvalidDataAccessResourceUsageException("test db error"));
            when(kycMessages.getMessage(MSG_APP_010))
                    .thenReturn(new MessageData());

            tokenService.readToken("someToken");
        });
        Assertions.assertTrue(ex.getException() instanceof DataAccessException);
    }

}
