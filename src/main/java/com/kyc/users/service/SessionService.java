package com.kyc.users.service;

import com.kyc.core.exception.KycRestException;
import com.kyc.core.properties.KycMessages;
import com.kyc.core.util.DateUtil;
import com.kyc.users.entity.KycLoginHistoric;
import com.kyc.users.entity.KycLoginUserInfo;
import com.kyc.users.entity.KycUser;
import com.kyc.users.model.SessionData;
import com.kyc.users.repositories.KycUserRepository;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

import static com.kyc.users.constants.AppConstants.MSG_APP_003;

@Service
public class SessionService {

    @Autowired
    private HistoricLoginService historicLoginService;

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
            KycLoginUserInfo loginUserInfo = user.getLoginUserInfo();

            if(loginUserInfo==null){

                loginUserInfo = new KycLoginUserInfo();
                loginUserInfo.setDateFirstLogin(sessionData.getNewDate());
                loginUserInfo.setUser(user);
                user.setLoginUserInfo(loginUserInfo);

            }

            loginUserInfo.setDateLastSuccessfulLogin(sessionData.getNewDate());
            loginUserInfo.setNumFailAttemptsCurrentLogin(0);

            kycUserRepository.save(user);
            historicLoginService.addHistoricLoginData(sessionData);
        }
        catch(DataAccessException ex){

            throw KycRestException.builderRestException()
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .errorData(kycMessages.getMessage(MSG_APP_003))
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
                    .errorData(kycMessages.getMessage(MSG_APP_003))
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
                    .errorData(kycMessages.getMessage(MSG_APP_003))
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
                     .errorData(kycMessages.getMessage(MSG_APP_003))
                     .exception(ex)
                     .build();
         }
    }

    private boolean checkTimeCurrentSession(KycLoginHistoric currentSession){

        Date lastLoginDate = currentSession.getDateLogin();
        LocalDateTime dateTime = DateUtil.dateToLocalDateTime(lastLoginDate).plusMinutes(15);

        LocalDateTime currentDateTime = LocalDateTime.now(clock);
        boolean cond1 = dateTime.isAfter(currentDateTime);
        boolean cond2 = dateTime.isEqual(currentDateTime);
        return BooleanUtils.or(new boolean[]{cond1,cond2});
    }
}
