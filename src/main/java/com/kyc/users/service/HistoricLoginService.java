package com.kyc.users.service;

import com.kyc.users.entity.KycLoginHistoric;
import com.kyc.users.model.SessionData;
import com.kyc.users.repositories.KycLoginHistoricRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HistoricLoginService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoricLoginService.class);

    @Autowired
    private KycLoginHistoricRepository kycLoginHistoricRepository;

    public void addHistoricLoginData(SessionData sessionData){

        String sessionId = sessionData.getSessionId();
        Long idUser = sessionData.getKycUser().getId();

        KycLoginHistoric loginHistoric = new KycLoginHistoric();
        loginHistoric.setUser(sessionData.getKycUser());
        loginHistoric.setIp(sessionData.getIp());
        loginHistoric.setIdChannel(sessionData.getIdChannel());
        loginHistoric.setIdSession(sessionId);
        loginHistoric.setActiveSession(true);
        loginHistoric.setDateLogin(sessionData.getNewDate());
        loginHistoric.setDateCheckpoint(sessionData.getNewDate());

        LOGGER.info("Saving the session {} in database",sessionId);
        KycLoginHistoric result = kycLoginHistoricRepository.save(loginHistoric);
        LOGGER.info("The session {} was generated for the user {}",sessionId,idUser);
    }

    public void addHistoricLogoutData(SessionData sessionData){

        Optional<KycLoginHistoric> opHistoricLogin = getCurrentSession(sessionData);
        if(opHistoricLogin.isPresent()){

            KycLoginHistoric loginHistoric = opHistoricLogin.get();

            String sessionId = loginHistoric.getIdSession();
            Long idUser = loginHistoric.getUser().getId();

            loginHistoric.setActiveSession(false);
            loginHistoric.setDateLogout(sessionData.getNewDate());
            LOGGER.info("Canceling the session {} in database",loginHistoric.getIdSession());
            kycLoginHistoricRepository.save(loginHistoric);
            LOGGER.info("The session {} was inactivated for the user {}",sessionId,idUser);
        }
    }

    public void refreshCheckpoint(SessionData sessionData){

        Optional<KycLoginHistoric> opHistoricLogin = getCurrentSession(sessionData);
        if(opHistoricLogin.isPresent()){

            KycLoginHistoric loginHistoric = opHistoricLogin.get();

            String sessionId = loginHistoric.getIdSession();
            Long idUser = loginHistoric.getUser().getId();

            loginHistoric.setDateCheckpoint(sessionData.getNewDate());
            LOGGER.info("Refreshing the session {}",sessionId);
            kycLoginHistoricRepository.save(loginHistoric);
            LOGGER.info("The session {} was refreshed for the user {}",sessionId,idUser);
        }
    }

    public Optional<KycLoginHistoric> getCurrentSession(SessionData sessionData){
        LOGGER.info("Retrieving the session {} in database",sessionData.getSessionId());
        return kycLoginHistoricRepository.getCurrentSession(sessionData.getSessionId());
    }

    public Optional<KycLoginHistoric> getCurrentSessionOnChannel(Long idUser, Integer idChannel){
        LOGGER.info("Retrieving the current session in database through the channel {} and user id {}",idUser,idChannel);
        return kycLoginHistoricRepository.getCurrentSessionOnChannel(idUser,idChannel);
    }

    public List<KycLoginHistoric> getActiveSessions(){
        return kycLoginHistoricRepository.getActiveSessions();
    }
}
