package com.kyc.users.service;

import com.kyc.core.exception.KycRestException;
import com.kyc.core.properties.KycMessages;
import com.kyc.users.entity.KycParameter;
import com.kyc.users.repositories.KycParameterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.kyc.users.constants.AppConstants.MSG_APP_003;

@Service
public class ParameterService {

    @Autowired
    private KycParameterRepository kycParameterRepository;

    @Autowired
    private KycMessages kycMessages;

    public KycParameter getParameter(String key){

        try{

            Optional<KycParameter> opKey = kycParameterRepository.getKey(key);
            return opKey.orElseThrow(()->
                    KycRestException.builderRestException()
                            .status(HttpStatus.SERVICE_UNAVAILABLE)
                            .errorData(kycMessages.getMessage(MSG_APP_003))
                            .inputData(key)
                            .build());
        }
        catch(DataAccessException ex){

            throw KycRestException.builderRestException()
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .errorData(kycMessages.getMessage(MSG_APP_003))
                    .exception(ex)
                    .inputData(key)
                    .build();
        }
    }
}
