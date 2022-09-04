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

        KycLoginHistoric loginHistoric = new KycLoginHistoric();
        loginHistoric.setUser(sessionData.getKycUser());
        loginHistoric.setIp(sessionData.getIp());
        loginHistoric.setIdChannel(sessionData.getIdChannel());
        loginHistoric.setIdSession(sessionData.getSessionId());
        loginHistoric.setActiveSession(true);
        loginHistoric.setDateLogin(sessionData.getNewDate());
        loginHistoric.setDateCheckpoint(sessionData.getNewDate());

        kycLoginHistoricRepository.save(loginHistoric);
    }

    public void addHistoricLogoutData(SessionData sessionData){

        Optional<KycLoginHistoric> opHistoricLogin = getCurrentSession(sessionData);
        if(opHistoricLogin.isPresent()){

            KycLoginHistoric loginHistoric = opHistoricLogin.get();
            loginHistoric.setActiveSession(false);
            loginHistoric.setDateLogout(sessionData.getNewDate());
            kycLoginHistoricRepository.save(loginHistoric);
        }
    }

    public void refreshCheckpoint(SessionData sessionData){

        Optional<KycLoginHistoric> opHistoricLogin = getCurrentSession(sessionData);
        if(opHistoricLogin.isPresent()){

            KycLoginHistoric loginHistoric = opHistoricLogin.get();
            loginHistoric.setDateCheckpoint(sessionData.getNewDate());
            kycLoginHistoricRepository.save(loginHistoric);
        }
    }

    public Optional<KycLoginHistoric> getCurrentSession(SessionData sessionData){
        return kycLoginHistoricRepository.getCurrentSession(sessionData.getSessionId());
    }

    public Optional<KycLoginHistoric> getCurrentSessionOnChannel(Long idUser, Integer idChannel){
        return kycLoginHistoricRepository.getCurrentSessionOnChannel(idUser,idChannel);
    }

    public List<KycLoginHistoric> getActiveSessions(){
        return kycLoginHistoricRepository.getActiveSessions();
    }
}
