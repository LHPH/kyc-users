package com.kyc.users.service;

import com.kyc.core.exception.KycRestException;
import com.kyc.core.properties.KycMessages;
import com.kyc.core.util.DateUtil;
import com.kyc.users.entity.KycLoginHistoric;
import com.kyc.users.entity.KycLoginUserInfo;
import com.kyc.users.entity.KycParameter;
import com.kyc.users.entity.KycUser;
import com.kyc.users.model.SessionData;
import com.kyc.users.repositories.KycUserRepository;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.kyc.users.constants.AppConstants.KYC_SESSION_TIMEOUT;
import static com.kyc.users.constants.AppConstants.MSG_APP_010;

@Service
public class SessionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionService.class);

    @Autowired
    private HistoricLoginService historicLoginService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private KycUserRepository kycUserRepository;

    @Autowired
    private KycMessages kycMessages;

    @Autowired
    private Clock clock;

    @Transactional
    public void openSession(SessionData sessionData){

        try{

            KycUser user = sessionData.getKycUser();
            LOGGER.info("Opening a new session for the user {}",user.getId());
            KycLoginUserInfo loginUserInfo = user.getLoginUserInfo();

            if(loginUserInfo==null){

                LOGGER.warn("The user {} does not have a login user info, creating data",user.getId());
                loginUserInfo = new KycLoginUserInfo();
                loginUserInfo.setDateFirstLogin(sessionData.getNewDate());
                loginUserInfo.setUser(user);
                user.setLoginUserInfo(loginUserInfo);

            }

            LOGGER.info("Updating login user info for the user {}",user.getId());
            loginUserInfo.setDateLastSuccessfulLogin(sessionData.getNewDate());
            loginUserInfo.setNumFailAttemptsCurrentLogin(0);

            kycUserRepository.save(user);
            historicLoginService.addHistoricLoginData(sessionData);
        }
        catch(DataAccessException ex){

            throw KycRestException.builderRestException()
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .errorData(kycMessages.getMessage(MSG_APP_010))
                    .exception(ex)
                    .build();
        }
    }

    @Transactional
    public void closeSession(SessionData sessionData){

        try{
            if(hasActiveSession(sessionData)){
                historicLoginService.addHistoricLogoutData(sessionData);
            }
        }
        catch(DataAccessException ex){

            throw KycRestException.builderRestException()
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .errorData(kycMessages.getMessage(MSG_APP_010))
                    .exception(ex)
                    .build();
        }
    }

    @Transactional
    public void closeIdleActiveSessions(){

        try{

            LOGGER.info("Retrieving active sessions");
            List<KycLoginHistoric> idleSessions = historicLoginService.getActiveSessions()
                    .stream().filter( e -> !checkTimeCurrentSession(e))
                    .collect(Collectors.toList());
            LOGGER.info("There are {} idle sessions",idleSessions.size());
            idleSessions.forEach(idle -> {

                SessionData sessionData = SessionData.builder()
                        .sessionId(idle.getIdSession())
                        .newDate(new Date())
                        .build();
                historicLoginService.addHistoricLogoutData(sessionData);
            });
            LOGGER.info("Finish the auto close idle sessions");
        }
        catch(DataAccessException ex){

            throw KycRestException.builderRestException()
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .errorData(kycMessages.getMessage(MSG_APP_010))
                    .exception(ex)
                    .build();
        }
    }

    @Transactional
    public boolean renewSession(SessionData sessionData){

        try{

            if(hasActiveSession(sessionData)){

                historicLoginService.refreshCheckpoint(sessionData);
                return true;
            }
            return false;
        }
        catch(DataAccessException ex){

            throw KycRestException.builderRestException()
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .errorData(kycMessages.getMessage(MSG_APP_010))
                    .exception(ex)
                    .build();
        }
    }

    public boolean hasActiveSession(SessionData sessionData){

        try{

            Optional<KycLoginHistoric> opCurrentSession = historicLoginService.getCurrentSession(sessionData);
            return opCurrentSession.filter(this::checkTimeCurrentSession)
                    .isPresent();
        }
        catch(DataAccessException ex){

            throw KycRestException.builderRestException()
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .errorData(kycMessages.getMessage(MSG_APP_010))
                    .exception(ex)
                    .build();
        }
    }

    public boolean hasActiveSessionOnChannel(Long idUser, Integer idChannel){

         try{
             Optional<KycLoginHistoric> opCurrentSession = historicLoginService.getCurrentSessionOnChannel(idUser,idChannel);
             return opCurrentSession.filter(this::checkTimeCurrentSession)
                     .isPresent();
         }
         catch(DataAccessException ex){
             throw KycRestException.builderRestException()
                     .status(HttpStatus.SERVICE_UNAVAILABLE)
                     .errorData(kycMessages.getMessage(MSG_APP_010))
                     .exception(ex)
                     .build();
         }
    }

    private boolean checkTimeCurrentSession(KycLoginHistoric currentSession){

        KycParameter kycParameter = parameterService.getParameter(KYC_SESSION_TIMEOUT);
        int sessionTimeout = NumberUtils.toInt(kycParameter.getValue(),15);

        Date dateCheckpoint = ObjectUtils.defaultIfNull(currentSession.getDateCheckpoint(),currentSession.getDateLogin());
        LocalDateTime dateTime = DateUtil.dateToLocalDateTime(dateCheckpoint).plusMinutes(sessionTimeout);

        LocalDateTime currentDateTime = LocalDateTime.now(clock);
        boolean cond1 = dateTime.isAfter(currentDateTime);
        boolean cond2 = dateTime.isEqual(currentDateTime);
        return BooleanUtils.or(new boolean[]{cond1,cond2});
    }
}
