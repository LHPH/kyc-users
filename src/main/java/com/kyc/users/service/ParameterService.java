package com.kyc.users.service;

import com.kyc.core.exception.KycRestException;
import com.kyc.core.properties.KycMessages;
import com.kyc.users.entity.KycParameter;
import com.kyc.users.repositories.KycParameterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.kyc.users.constants.AppConstants.MSG_APP_010;

@Service
public class ParameterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParameterService.class);

    @Autowired
    private KycParameterRepository kycParameterRepository;

    @Autowired
    private KycMessages kycMessages;

    @Cacheable("parameters")
    public KycParameter getParameter(String key){

        try{
            LOGGER.info("Retrieving the parameter {}",key);
            Optional<KycParameter> opKey = kycParameterRepository.getKey(key);
            return opKey.orElseThrow(()->
                    KycRestException.builderRestException()
                            .status(HttpStatus.SERVICE_UNAVAILABLE)
                            .errorData(kycMessages.getMessage(MSG_APP_010))
                            .inputData(key)
                            .build());
        }
        catch(DataAccessException ex){

            throw KycRestException.builderRestException()
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .errorData(kycMessages.getMessage(MSG_APP_010))
                    .exception(ex)
                    .inputData(key)
                    .build();
        }
    }
}
