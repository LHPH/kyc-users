package com.kyc.users.service;

import com.kyc.core.exception.KycRestException;
import com.kyc.core.model.jwt.JWTData;
import com.kyc.core.model.jwt.TokenData;
import com.kyc.core.model.web.MessageData;
import com.kyc.core.model.web.RequestData;
import com.kyc.core.model.web.ResponseData;
import com.kyc.core.properties.KycMessages;
import com.kyc.core.services.PasswordEncoderService;
import com.kyc.users.entity.KycLoginUserInfo;
import com.kyc.users.entity.KycParameter;
import com.kyc.users.entity.KycUser;
import com.kyc.users.entity.KycUserRelation;
import com.kyc.users.entity.KycUserType;
import com.kyc.users.enums.KycUserTypeEnum;
import com.kyc.users.model.CredentialData;
import com.kyc.users.model.SessionData;
import com.kyc.users.repositories.KycUserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.Clock;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.kyc.users.constants.AppConstants.CHANNEL;
import static com.kyc.users.constants.AppConstants.IP;
import static com.kyc.users.constants.AppConstants.KYC_FAIL_LOGIN_ATTEMPTS;
import static com.kyc.users.constants.AppConstants.MSG_APP_006;
import static com.kyc.users.constants.AppConstants.MSG_APP_007;
import static com.kyc.users.constants.AppConstants.MSG_APP_008;
import static com.kyc.users.constants.AppConstants.MSG_APP_009;
import static com.kyc.users.constants.AppConstants.MSG_APP_011;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserAuthServiceTest {

    @Mock
    private KycUserRepository kycUserRepository;

    @Mock
    private PasswordEncoderService passwordEncoderService;

    @Mock
    private SessionService sessionService;

    @Mock
    private TokenService tokenService;

    @Mock
    private ParameterService parameterService;

    @Mock
    private Clock clock;

    @Mock
    private KycMessages kycMessages;

    @InjectMocks
    private UserAuthService service;

    private RequestData<CredentialData> req;

    @BeforeAll
    public static void setUp(){
        MockitoAnnotations.openMocks(UserAuthServiceTest.class);
    }

    @BeforeEach
    public void init(){

        Map<String,Object> headers = new HashMap<>();
        headers.put(IP,"0.0.0.0");
        headers.put(CHANNEL,"1");

        CredentialData credentialData = new CredentialData("test","test");
        req = RequestData.<CredentialData>builder()
                .headers(headers)
                .body(credentialData)
                .build();
    }

    @Test
    public void signInUser_signingInUser_successfulSignIn(){

        KycUserType userType = new KycUserType();
        userType.setId(KycUserTypeEnum.CUSTOMER.getId());

        KycUser user = new KycUser();
        user.setId(1L);
        user.setUsername("user");
        user.setSecret("user");
        user.setActive(true);
        user.setLocked(false);
        user.setUserRelation(new KycUserRelation());
        user.getUserRelation().setUserType(userType);

        when(kycUserRepository.findByUsername(anyString()))
                .thenReturn(Optional.of(user));
        when(passwordEncoderService.matches(anyString(),anyString()))
                .thenReturn(true);
        when(sessionService.hasActiveSessionOnChannel(anyLong(),anyInt()))
                .thenReturn(false);
        when(tokenService.getToken(any(JWTData.class)))
                .thenReturn("token");

        service.signInUser(req);
        verify(sessionService,times(1)).openSession(any(SessionData.class));

    }

    @Test
    public void signInUser_signingInUserButBadPasswordAndNoLoginUserInfo_unsuccessfulSignInAndAddAttempt(){

        KycUserType userType = new KycUserType();
        userType.setId(KycUserTypeEnum.CUSTOMER.getId());

        KycUser user = new KycUser();
        user.setId(1L);
        user.setUsername("user");
        user.setSecret("user");
        user.setActive(true);
        user.setLocked(false);
        user.setUserRelation(new KycUserRelation());
        user.getUserRelation().setUserType(userType);

        when(kycUserRepository.findByUsername(anyString()))
                .thenReturn(Optional.of(user));
        when(passwordEncoderService.matches(anyString(),anyString()))
                .thenReturn(false);

        when(parameterService.getParameter(KYC_FAIL_LOGIN_ATTEMPTS))
                .thenReturn(new KycParameter("KEY","3"));
        when(kycMessages.getMessage(MSG_APP_006))
                .thenReturn(new MessageData());

        ResponseData<TokenData> response = service.signInUser(req);
        verify(sessionService,times(0)).openSession(any(SessionData.class));
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED,response.getHttpStatus());

    }

    @Test
    public void signInUser_multipleFailedAttemptsToLogin_unsuccessfulSignInAndLockUser(){

        KycUserType userType = new KycUserType();
        userType.setId(KycUserTypeEnum.CUSTOMER.getId());

        KycUser user = new KycUser();
        user.setId(1L);
        user.setUsername("user");
        user.setSecret("user");
        user.setActive(true);
        user.setLocked(false);
        user.setUserRelation(new KycUserRelation());
        user.getUserRelation().setUserType(userType);

        KycLoginUserInfo loginUserInfo = new KycLoginUserInfo();
        loginUserInfo.setNumFailAttemptsCurrentLogin(2);

        when(kycUserRepository.findByUsername(anyString()))
                .thenReturn(Optional.of(user));
        when(passwordEncoderService.matches(anyString(),anyString()))
                .thenReturn(false);

        when(parameterService.getParameter(KYC_FAIL_LOGIN_ATTEMPTS))
                .thenReturn(new KycParameter("KEY","3"));
        when(kycMessages.getMessage(MSG_APP_006))
                .thenReturn(new MessageData());

        ResponseData<TokenData> response = service.signInUser(req);
        verify(sessionService,times(0)).openSession(any(SessionData.class));
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED,response.getHttpStatus());

    }

    @Test
    public void signInUser_signingInUserButLockedUser_unsuccessfulSignIn(){

        KycRestException ex = Assertions.assertThrows(KycRestException.class,()-> {
            KycUserType userType = new KycUserType();
            userType.setId(KycUserTypeEnum.CUSTOMER.getId());

            KycUser user = new KycUser();
            user.setId(1L);
            user.setUsername("user");
            user.setSecret("user");
            user.setActive(true);
            user.setLocked(true);
            user.setUserRelation(new KycUserRelation());
            user.getUserRelation().setUserType(userType);

            when(kycUserRepository.findByUsername(anyString()))
                    .thenReturn(Optional.of(user));
            when(passwordEncoderService.matches(anyString(), anyString()))
                    .thenReturn(true);

            when(kycMessages.getMessage(MSG_APP_008)).thenReturn(new MessageData());

            service.signInUser(req);
        });
        Assertions.assertEquals(HttpStatus.FORBIDDEN,ex.getStatus());
    }

    @Test
    public void signInUser_signingInUserButDisabledUser_unsuccessfulSignIn(){

        KycRestException ex = Assertions.assertThrows(KycRestException.class,()->{

            KycUserType userType = new KycUserType();
            userType.setId(KycUserTypeEnum.CUSTOMER.getId());

            KycUser user = new KycUser();
            user.setId(1L);
            user.setUsername("user");
            user.setSecret("user");
            user.setActive(false);
            user.setLocked(false);
            user.setUserRelation(new KycUserRelation());
            user.getUserRelation().setUserType(userType);

            when(kycUserRepository.findByUsername(anyString()))
                    .thenReturn(Optional.of(user));
            when(passwordEncoderService.matches(anyString(),anyString()))
                    .thenReturn(true);

            when(kycMessages.getMessage(MSG_APP_008)).thenReturn(new MessageData());

            service.signInUser(req);
        });
        Assertions.assertEquals(HttpStatus.FORBIDDEN,ex.getStatus());
    }

    @Test
    public void signInUser_signingInUserButAlreadyAuthenticateOnChannel_unsuccessfulSignIn(){

        KycRestException ex = Assertions.assertThrows(KycRestException.class,()->{

            KycUserType userType = new KycUserType();
            userType.setId(KycUserTypeEnum.CUSTOMER.getId());

            KycUser user = new KycUser();
            user.setId(1L);
            user.setUsername("user");
            user.setSecret("user");
            user.setActive(true);
            user.setLocked(false);
            user.setUserRelation(new KycUserRelation());
            user.getUserRelation().setUserType(userType);

            when(kycUserRepository.findByUsername(anyString()))
                    .thenReturn(Optional.of(user));
            when(passwordEncoderService.matches(anyString(),anyString()))
                    .thenReturn(true);
            when(sessionService.hasActiveSessionOnChannel(anyLong(),anyInt()))
                    .thenReturn(true);

            when(kycMessages.getMessage(MSG_APP_009)).thenReturn(new MessageData());

            service.signInUser(req);
        });
        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY,ex.getStatus());
    }

    @Test
    public void signInUser_signingInUserNotAllowedToAuth_unsuccessfulSignIn(){

        KycRestException ex = Assertions.assertThrows(KycRestException.class,()->{

            KycUserType userType = new KycUserType();
            userType.setId(KycUserTypeEnum.SYSTEM.getId());

            KycUser user = new KycUser();
            user.setId(1L);
            user.setUsername("user");
            user.setSecret("user");
            user.setActive(true);
            user.setLocked(false);
            user.setUserRelation(new KycUserRelation());
            user.getUserRelation().setUserType(userType);

            when(kycUserRepository.findByUsername(anyString()))
                    .thenReturn(Optional.of(user));

            when(kycMessages.getMessage(MSG_APP_007)).thenReturn(new MessageData());

            service.signInUser(req);
        });
        Assertions.assertEquals(HttpStatus.FORBIDDEN,ex.getStatus());
    }

    @Test
    public void signOutUser_signOutUser_successfulSigningOut(){

        RequestData<Void> req = RequestData.<Void>builder()
                .headers(Collections.singletonMap("Authorization","token"))
                .build();

        when(tokenService.readToken(anyString()))
                .thenReturn(new JWTData());

        service.signOutUser(req);
        verify(sessionService,times(1)).closeSession(any(SessionData.class));

    }

    @Test
    public void renewSession_renewingValidSession_successfulRenewal(){

        RequestData<Void> req = RequestData.<Void>builder()
                .headers(Collections.singletonMap("Authorization","token"))
                .build();

        when(tokenService.readToken(anyString()))
                .thenReturn(new JWTData());
        when(sessionService.renewSession(any(SessionData.class)))
                .thenReturn(true);

        ResponseData<Void> response = service.renewSession(req);
        Assertions.assertEquals(HttpStatus.OK,response.getHttpStatus());
    }

    @Test
    public void renewSession_renewingInvalidValidSession_unsuccessfulRenewal(){

        RequestData<Void> req = RequestData.<Void>builder()
                .headers(Collections.singletonMap("Authorization","token"))
                .build();

        when(tokenService.readToken(anyString()))
                .thenReturn(new JWTData());
        when(sessionService.renewSession(any(SessionData.class)))
                .thenReturn(false);
        when(kycMessages.getMessage(MSG_APP_011))
                .thenReturn(new MessageData());

        ResponseData<Void> response = service.renewSession(req);
        Assertions.assertEquals(HttpStatus.FORBIDDEN,response.getHttpStatus());
    }
}
