package com.kyc.users.service;

import com.kyc.core.exception.KycRestException;
import com.kyc.core.model.jwt.JWTData;
import com.kyc.core.model.jwt.TokenData;
import com.kyc.core.model.web.RequestData;
import com.kyc.core.model.web.ResponseData;
import com.kyc.core.properties.KycMessages;
import com.kyc.core.services.PasswordEncoderService;
import com.kyc.users.entity.KycLoginUserInfo;
import com.kyc.users.entity.KycParameter;
import com.kyc.users.entity.KycUser;
import com.kyc.users.entity.KycUserRelation;
import com.kyc.users.enums.KycUserTypeEnum;
import com.kyc.users.model.CredentialData;
import com.kyc.users.model.SessionData;
import com.kyc.users.repositories.KycUserRepository;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.kyc.users.constants.AppConstants.CHANNEL;
import static com.kyc.users.constants.AppConstants.IP;
import static com.kyc.users.constants.AppConstants.KYC_FAIL_LOGIN_ATTEMPTS;
import static com.kyc.users.constants.AppConstants.MSG_APP_006;
import static com.kyc.users.constants.AppConstants.MSG_APP_007;
import static com.kyc.users.constants.AppConstants.MSG_APP_008;
import static com.kyc.users.constants.AppConstants.MSG_APP_009;
import static com.kyc.users.constants.AppConstants.MSG_APP_011;

@Service
public class UserAuthService {

    @Autowired
    private KycUserRepository kycUserRepository;

    @Autowired
    private PasswordEncoderService passwordEncoderService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private Clock clock;

    @Autowired
    private KycMessages kycMessages;

    private static final Set<KycUserTypeEnum> ALLOWED_TYPES;

    static{

        Set<KycUserTypeEnum> allowedTypes = new HashSet<>();
        allowedTypes.add(KycUserTypeEnum.CUSTOMER);
        allowedTypes.add(KycUserTypeEnum.EXECUTIVE);
        ALLOWED_TYPES = Collections.unmodifiableSet(allowedTypes);
    }

    @Transactional
    public ResponseData<TokenData> signInUser(RequestData<CredentialData> req){

        CredentialData credentialData = req.getBody();
        Integer idChannel = NumberUtils.toInt(String.valueOf((req.getHeaders().get(CHANNEL))));
        String ip = String.valueOf(req.getHeaders().get(IP));

        Optional<KycUser> opUser = kycUserRepository.findByUsername(credentialData.getUsername());

        if(opUser.isPresent()){

            KycUser user = opUser.get();
            String pass = user.getSecret();
            checkValidTypeUser(user);

            if(passwordEncoderService.matches(credentialData.getPassword(),pass)){

                SessionData sessionData = SessionData.builder()
                        .kycUser(user)
                        .ip(ip)
                        .idChannel(idChannel)
                        .sessionId(UUID.randomUUID().toString())
                        .newDate(new Date())
                        .build();

                checkEnabledUser(user);
                checkNoCurrentSessionOnChannel(user,idChannel);

                sessionService.openSession(sessionData);

                JWTData jwtData = new JWTData();
                jwtData.setChannel(String.valueOf(idChannel));
                jwtData.setIssuer("kyc-users");
                jwtData.setSubject(String.valueOf(user.getId()));
                jwtData.setKey(sessionData.getSessionId());
                jwtData.setAudience(String.valueOf(user.getUserRelation().getUserType().getId()));
               // jwtData.setExpirationTime(DateUtil.localDateTimeToDate(LocalDateTime.now(clock).plusMinutes(15)));

                return ResponseData.of(new TokenData(tokenService.getToken(jwtData)));
            }
            else{

                failLoginActions(user);
            }
        }
        throw KycRestException.builderRestException()
                .status(HttpStatus.UNAUTHORIZED)
                .errorData(kycMessages.getMessage(MSG_APP_006))
                .inputData(req)
                .build();
    }

    @Transactional
    public ResponseData<Void> signOutUser(RequestData<Void> req){

        Map<String,Object> map = req.getHeaders();
        String token = Objects.toString(map.get(HttpHeaders.AUTHORIZATION));

        JWTData data = tokenService.readToken(token);
        String key = data.getKey();

        SessionData sessionData = SessionData.builder()
                .sessionId(key)
                .newDate(new Date())
                .build();

        sessionService.closeSession(sessionData);

        return ResponseData.emptyResponse();
    }

    @Transactional
    public ResponseData<Void> renewSession(RequestData<Void> req){

        Map<String,Object> map = req.getHeaders();
        String token = Objects.toString(map.get(HttpHeaders.AUTHORIZATION));

        JWTData data = tokenService.readToken(token);
        String key = data.getKey();

        SessionData sessionData = SessionData.builder()
                .sessionId(key)
                .newDate(new Date())
                .build();
        boolean isRenewed = sessionService.renewSession(sessionData);

        if(isRenewed){

            return ResponseData.emptyResponse();
        }
        else{

            sessionService.closeSession(sessionData);
            return ResponseData.of(kycMessages.getMessage(MSG_APP_011),HttpStatus.FORBIDDEN);
        }
    }

    private void failLoginActions(KycUser user){

        KycLoginUserInfo loginUserInfo = user.getLoginUserInfo();
        if(loginUserInfo==null){

            loginUserInfo = new KycLoginUserInfo();
            loginUserInfo.setUser(user);
            user.setLoginUserInfo(loginUserInfo);
        }

        Integer failAttemptsToLogin = ObjectUtils.defaultIfNull(loginUserInfo.getNumFailAttemptsCurrentLogin(),0);

        Date newDate = new Date();
        loginUserInfo.setDateLastFailureLogin(newDate);
        loginUserInfo.setNumFailAttemptsCurrentLogin(failAttemptsToLogin + 1);

        KycParameter kycParameter = parameterService.getParameter(KYC_FAIL_LOGIN_ATTEMPTS);
        int maxFailAttempts = NumberUtils.toInt(kycParameter.getValue(),3);

        if(loginUserInfo.getNumFailAttemptsCurrentLogin()>maxFailAttempts){

            user.setLocked(true);
            loginUserInfo.setDateLockedUser(newDate);
        }

        kycUserRepository.save(user);

        throw KycRestException.builderRestException()
                .status(HttpStatus.UNAUTHORIZED)
                .errorData(kycMessages.getMessage(MSG_APP_006))
                .inputData(user.getId())
                .build();
    }

    private void checkValidTypeUser(KycUser user){

        KycUserRelation relation = user.getUserRelation();
        KycUserTypeEnum type = KycUserTypeEnum.getInstanceById(relation.getUserType().getId());

        if(!ALLOWED_TYPES.contains(type)){

            throw KycRestException.builderRestException()
                    .status(HttpStatus.FORBIDDEN)
                    .errorData(kycMessages.getMessage(MSG_APP_007))
                    .inputData(user.getId())
                    .build();
        }

    }

    private void checkEnabledUser(KycUser user){

        if(!Boolean.TRUE.equals(user.getActive())){

            throw KycRestException.builderRestException()
                    .status(HttpStatus.FORBIDDEN)
                    .errorData(kycMessages.getMessage(MSG_APP_008))
                    .inputData(user.getId())
                    .build();
        }

        if(Boolean.TRUE.equals(user.getLocked())){

            throw KycRestException.builderRestException()
                    .status(HttpStatus.FORBIDDEN)
                    .errorData(kycMessages.getMessage(MSG_APP_008))
                    .inputData(user.getId())
                    .build();
        }
    }

    private void checkNoCurrentSessionOnChannel(KycUser user, Integer idChannel){

        boolean hasSession = sessionService.hasActiveSessionOnChannel(user.getId(),idChannel);
        if(hasSession){

            throw KycRestException.builderRestException()
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .errorData(kycMessages.getMessage(MSG_APP_009))
                    .inputData(user.getId())
                    .build();
        }

    }



}
