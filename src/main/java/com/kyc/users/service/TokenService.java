package com.kyc.users.service;

import com.kyc.core.exception.KycRestException;
import com.kyc.core.model.jwt.JWTData;
import com.kyc.core.properties.KycMessages;
import com.kyc.core.util.DateUtil;
import com.kyc.core.util.TokenUtil;
import com.kyc.users.entity.KycParameter;
import com.kyc.users.repositories.KycParameterRepository;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static com.kyc.users.constants.AppConstants.MSG_APP_003;

@Service
public class TokenService {

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private KycMessages kycMessages;

    public String getToken(JWTData jwtData){

        try{
            String secret = parameterService.getParameter("").getKey();

            return TokenUtil.getToken(jwtData, JWSAlgorithm.HS256,secret.getBytes(StandardCharsets.UTF_8));
        }
        catch(JOSEException | DataAccessException ex){

            throw KycRestException.builderRestException()
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .errorData(kycMessages.getMessage(MSG_APP_003))
                    .exception(ex)
                    .inputData(jwtData)
                    .build();
        }
    }
}
