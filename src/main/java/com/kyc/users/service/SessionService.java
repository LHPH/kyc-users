package com.kyc.users.service;

import com.kyc.core.properties.KycMessages;
import com.kyc.core.util.DateUtil;
import com.kyc.users.aspects.DatabaseHandlingException;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.kyc.users.constants.AppConstants.KYC_SESSION_TIMEOUT;

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
    @DatabaseHandlingException
    public void openSession(SessionData sessionData){

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

    @Transactional
    @DatabaseHandlingException
    public void closeSession(SessionData sessionData){

        if(hasActiveSession(sessionData)){
            historicLoginService.addHistoricLogoutData(sessionData);
        }
    }

    @Transactional
    @DatabaseHandlingException
    public void closeIdleActiveSessions(){

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

    @Transactional
    @DatabaseHandlingException
    public boolean renewSession(SessionData sessionData){

        if(hasActiveSession(sessionData)){

            historicLoginService.refreshCheckpoint(sessionData);
            return true;
        }
        return false;
    }

    @DatabaseHandlingException
    public boolean hasActiveSession(SessionData sessionData){

        Optional<KycLoginHistoric> opCurrentSession = historicLoginService.getCurrentSession(sessionData);
        return opCurrentSession.filter(this::checkTimeCurrentSession)
                .isPresent();
    }

    @DatabaseHandlingException
    public boolean hasActiveSessionOnChannel(Long idUser, Integer idChannel){

        Optional<KycLoginHistoric> opCurrentSession = historicLoginService.getCurrentSessionOnChannel(idUser,idChannel);
        return opCurrentSession.filter(this::checkTimeCurrentSession)
                .isPresent();
    }

    private boolean checkTimeCurrentSession(KycLoginHistoric currentSession){

        KycParameter kycParameter = parameterService.getParameter(KYC_SESSION_TIMEOUT);
        int sessionTimeout = NumberUtils.toInt(kycParameter.getValue(),15);


        Date dateCheckpoint = ObjectUtils.defaultIfNull(currentSession.getDateCheckpoint(),currentSession.getDateLogin());
        LocalDateTime dateTimeCheckpoint = DateUtil.dateToLocalDateTime(dateCheckpoint);
        LocalDateTime dateTimeLimit = dateTimeCheckpoint.plusMinutes(sessionTimeout);

        LocalDateTime currentDateTime = LocalDateTime.now(clock);
        boolean cond1 = dateTimeCheckpoint.isEqual(currentDateTime);
        boolean cond2 = currentDateTime.isBefore(dateTimeLimit);
        boolean cond3 = currentDateTime.isEqual(dateTimeLimit);
        return BooleanUtils.or(new boolean[]{cond1,cond2,cond3});
    }
}
