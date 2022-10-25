package com.kyc.users.aspects;

import com.kyc.core.enums.MessageType;
import com.kyc.core.exception.KycRestException;
import com.kyc.core.model.web.MessageData;
import com.kyc.core.properties.KycMessages;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.InvalidDataAccessResourceUsageException;

import static com.kyc.users.constants.AppConstants.MSG_APP_010;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DatabaseExceptionAspectTest {

    @Mock
    private KycMessages kycMessages;

    @InjectMocks
    private DatabaseExceptionAspect aspect;

    @Test
    public void handleDatabaseException_executeJoinPoint_successfulExecution() throws Throwable {

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        when(joinPoint.proceed()).thenReturn(new Object());

        Assertions.assertNotNull(aspect.handleDatabaseException(joinPoint));
    }

    @Test
    public void handleDatabaseException_handleDatabaseException_throwKycRestException(){

        Assertions.assertThrows(KycRestException.class,()->{

            ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);

            when(joinPoint.proceed()).thenThrow(new InvalidDataAccessResourceUsageException("test error db"));
            when(kycMessages.getMessage(MSG_APP_010)).thenReturn(new MessageData("CODE","MESSAGE", MessageType.ERROR));

            aspect.handleDatabaseException(joinPoint);
        });
    }
}
