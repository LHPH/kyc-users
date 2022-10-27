package com.kyc.users.service;

import com.kyc.core.exception.KycRestException;
import com.kyc.core.model.jwt.JWTData;
import com.kyc.core.model.jwt.TokenData;
import com.kyc.core.model.web.RequestData;
import com.kyc.core.model.web.ResponseData;
import com.kyc.core.properties.KycMessages;
import com.kyc.core.services.PasswordEncoderService;
import com.kyc.users.aspects.DatabaseHandlingException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import static com.kyc.users.constants.AppConstants.KYC_USERS;
import static com.kyc.users.constants.AppConstants.MSG_APP_006;
import static com.kyc.users.constants.AppConstants.MSG_APP_007;
import static com.kyc.users.constants.AppConstants.MSG_APP_008;
import static com.kyc.users.constants.AppConstants.MSG_APP_009;
import static com.kyc.users.constants.AppConstants.MSG_APP_011;

@Service
public class UserAuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserAuthService.class);

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

    @Value("${kyc-config.token.audience.users}")
    private String tokenAudience;

    private static final Set<KycUserTypeEnum> ALLOWED_TYPES;

    static{

        Set<KycUserTypeEnum> allowedTypes = new HashSet<>();
        allowedTypes.add(KycUserTypeEnum.CUSTOMER);
        allowedTypes.add(KycUserTypeEnum.EXECUTIVE);
        ALLOWED_TYPES = Collections.unmodifiableSet(allowedTypes);
    }

    @Transactional
    @DatabaseHandlingException
    public ResponseData<TokenData> signInUser(RequestData<CredentialData> req){

        LOGGER.info("Starting process to sign-in the user");
        CredentialData credentialData = req.getBody();
        Integer idChannel = NumberUtils.toInt(String.valueOf((req.getHeaders().get(CHANNEL))));
        String ip = String.valueOf(req.getHeaders().get(IP));
        LOGGER.info("The user wants to auth since channel {} and ip {}",idChannel,ip);

        LOGGER.info("Checking if the user exists");
        Optional<KycUser> opUser = kycUserRepository.findByUsername(credentialData.getUsername());

        if(opUser.isPresent()){

            KycUser user = opUser.get();
            String pass = user.getSecret();
            LOGGER.info("Checking if the user type is valid");
            checkValidTypeUser(user);

            LOGGER.info("Checking if the user password is valid");
            if(passwordEncoderService.matches(credentialData.getPassword(),pass)){

                SessionData sessionData = SessionData.builder()
                        .kycUser(user)
                        .ip(ip)
                        .idChannel(idChannel)
                        .sessionId(UUID.randomUUID().toString())
                        .newDate(new Date())
                        .build();

                LOGGER.info("Checking if the user is enabled");
                checkEnabledUser(user);
                LOGGER.info("Checking if the user does not have a current session in the channel");
                checkNoCurrentSessionOnChannel(user,idChannel);

                LOGGER.info("The user pass all the validations, generating session");
                sessionService.openSession(sessionData);

                JWTData jwtData = new JWTData();
                jwtData.setChannel(String.valueOf(idChannel));
                jwtData.setIssuer(KYC_USERS);
                jwtData.setSubject(String.valueOf(user.getId()));
                jwtData.setKey(sessionData.getSessionId());
                jwtData.setAudience(tokenAudience);

                LOGGER.info("Generating and returning access token");
                return ResponseData.of(new TokenData(tokenService.getToken(jwtData)));
            }
            else{
                LOGGER.warn("The user credentials are invalid");
                return failLoginActions(user);
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

        LOGGER.info("Starting process to sign-out the user");
        Map<String,Object> map = req.getHeaders();
        String token = Objects.toString(map.get(HttpHeaders.AUTHORIZATION));

        LOGGER.info("Reading and validating access token");
        JWTData data = tokenService.readToken(token);
        String key = data.getKey();

        LOGGER.info("Retrieving session id from the token");
        SessionData sessionData = SessionData.builder()
                .sessionId(key)
                .newDate(new Date())
                .build();

        LOGGER.info("Closing session");
        sessionService.closeSession(sessionData);

        LOGGER.info("The user was signed-out");
        return ResponseData.emptyResponse();
    }

    @Transactional
    public ResponseData<Void> renewSession(RequestData<Void> req){

        LOGGER.info("Starting process to renew the user session");
        Map<String,Object> map = req.getHeaders();
        String token = Objects.toString(map.get(HttpHeaders.AUTHORIZATION));

        LOGGER.info("Reading and validating access token");
        JWTData data = tokenService.readToken(token);
        String key = data.getKey();

        LOGGER.info("Retrieving session id");
        SessionData sessionData = SessionData.builder()
                .sessionId(key)
                .newDate(new Date())
                .build();

        LOGGER.info("Checking if the session could be renewed");
        boolean isRenewed = sessionService.renewSession(sessionData);

        if(isRenewed){

            LOGGER.info("The session {} was renewed",key);
            return ResponseData.emptyResponse();
        }
        else{

            LOGGER.warn("The session {} could no be renewed",key);
            sessionService.closeSession(sessionData);
            return ResponseData.of(kycMessages.getMessage(MSG_APP_011),HttpStatus.FORBIDDEN);
        }
    }

    private ResponseData<TokenData> failLoginActions(KycUser user){

        KycLoginUserInfo loginUserInfo = user.getLoginUserInfo();
        if(loginUserInfo==null){

            loginUserInfo = new KycLoginUserInfo();
            loginUserInfo.setUser(user);
            user.setLoginUserInfo(loginUserInfo);
        }

        LOGGER.warn("Retrieving the failed attempts of the user {}",user.getId());
        Integer failAttemptsToLogin = ObjectUtils.defaultIfNull(loginUserInfo.getNumFailAttemptsCurrentLogin(),0);

        LOGGER.warn("Adding a new failed attempt");
        Date newDate = new Date();
        loginUserInfo.setDateLastFailureLogin(newDate);
        loginUserInfo.setNumFailAttemptsCurrentLogin(failAttemptsToLogin + 1);

        KycParameter kycParameter = parameterService.getParameter(KYC_FAIL_LOGIN_ATTEMPTS);
        int maxFailAttempts = NumberUtils.toInt(kycParameter.getValue(),3);

        LOGGER.warn("Checking number of failed attempts");
        if(loginUserInfo.getNumFailAttemptsCurrentLogin()>=maxFailAttempts){

            LOGGER.warn("The user {} exceed the allowed failed attempts, locking the user",user.getId());
            user.setLocked(true);
            user.setDateUpdated(new Date());
            loginUserInfo.setDateLockedUser(newDate);
        }

        LOGGER.warn("Updating the user login info of the user {}",user.getId());
        kycUserRepository.save(user);

        return ResponseData.<TokenData>builder()
                .httpStatus(HttpStatus.UNAUTHORIZED)
                .error(kycMessages.getMessage(MSG_APP_006))
                .data(null)
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
