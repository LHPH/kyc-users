package com.kyc.users.aspects;

import com.kyc.core.exception.KycRestException;
import com.kyc.core.properties.KycMessages;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Arrays;

import static com.kyc.users.constants.AppConstants.MSG_APP_010;

@Aspect
@Component
public class DatabaseExceptionAspect {

    @Autowired
    private KycMessages kycMessages;

    @Around("@annotation(DatabaseHandlingException)")
    public Object handleDatabaseException(ProceedingJoinPoint joinPoint) throws Throwable {

        try{
            return joinPoint.proceed();
        }
        catch(DataAccessException ex){

            throw KycRestException.builderRestException()
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .inputData(Arrays.toString(joinPoint.getArgs()))
                    .errorData(kycMessages.getMessage(MSG_APP_010))
                    .exception(ex)
                    .build();
        }
    }
}