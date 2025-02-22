package com.kyc.users.service;

import com.kyc.core.exception.KycRestException;
import com.kyc.core.model.MessageData;
import com.kyc.core.model.jwt.JwtData;
import com.kyc.core.persistence.entity.KycParameter;
import com.kyc.core.properties.KycMessages;
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
import java.util.Collections;

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

        JwtData jwtData = new JwtData();
        jwtData.setSub("sub");

        when(parameterService.getParameter(KYC_SHARED_KEY))
                .thenReturn(new KycParameter("key","12345678901234567890123456789012",null,null));

        String token = tokenService.getToken(jwtData);
        Assertions.assertNotNull(token);
    }

    @Test
    public void getToken_badRetrievedKey_throwException(){

        KycRestException ex = Assertions.assertThrows(KycRestException.class,()->{

            JwtData jwtData = new JwtData();
            jwtData.setSub("sub");

            when(parameterService.getParameter(KYC_SHARED_KEY))
                    .thenReturn(new KycParameter("","bad",null,null));
            when(kycMessages.getMessage(MSG_APP_010))
                    .thenReturn(new MessageData());
            tokenService.getToken(jwtData);
        });
        Assertions.assertTrue(ex.getException() instanceof JOSEException);
    }

    @Test
    public void getToken_unavailableDatabase_throwException(){

        KycRestException ex = Assertions.assertThrows(KycRestException.class,()->{

            JwtData jwtData = new JwtData();
            jwtData.setSub("sub");

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

        JwtData jwtData = new JwtData();
        jwtData.setSub("sub");
        jwtData.setChannel("channel");
        jwtData.setAud(Collections.singletonList("aud"));

        when(parameterService.getParameter(KYC_SHARED_KEY))
                .thenReturn(new KycParameter("key","12345678901234567890123456789012",null,null));

        String token = tokenService.getToken(jwtData);

        JwtData result = tokenService.readToken(token);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(jwtData.getSub(),result.getSub());
    }

    @Test
    public void readToken_badSharedKey_throwException(){

        KycRestException ex = Assertions.assertThrows(KycRestException.class,()->{

            when(parameterService.getParameter(KYC_SHARED_KEY))
                    .thenReturn(new KycParameter("key","badKey",null,null));
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
                    .thenReturn(new KycParameter("key","12345678901234567890123456789012",null,null));
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
