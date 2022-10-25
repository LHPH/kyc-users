package com.kyc.users.service;

import com.kyc.core.exception.KycRestException;
import com.kyc.core.model.web.MessageData;
import com.kyc.core.properties.KycMessages;
import com.kyc.users.delegate.UserDelegateTest;
import com.kyc.users.entity.KycParameter;
import com.kyc.users.repositories.KycParameterRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.InvalidDataAccessResourceUsageException;

import java.util.Optional;

import static com.kyc.users.constants.AppConstants.MSG_APP_010;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParameterServiceTest {

    @Mock
    private KycParameterRepository kycParameterRepository;

    @Mock
    private KycMessages kycMessages;

    @InjectMocks
    private ParameterService service;

    @BeforeAll
    public static void init(){
        MockitoAnnotations.openMocks(ParameterServiceTest.class);
    }

    @Test
    public void getParameter_retrieveParameter_returnParameterData(){

        KycParameter parameter = new KycParameter();
        parameter.setKey("PARAM");
        parameter.setValue("VALUE");

        when(kycParameterRepository.getKey(parameter.getKey()))
                .thenReturn(Optional.of(parameter));

        KycParameter result = service.getParameter("PARAM");
        Assertions.assertNotNull(result);
        Assertions.assertEquals(parameter.getValue(),result.getValue());
    }

    @Test
    public void getParameter_retrieveParameterButDatabaseUnavailable_throwException(){

        Assertions.assertThrows(KycRestException.class,()->{

            KycParameter parameter = new KycParameter();
            parameter.setKey("PARAM");
            parameter.setValue("VALUE");

            when(kycParameterRepository.getKey(parameter.getKey()))
                    .thenReturn(Optional.empty());
            when(kycMessages.getMessage(MSG_APP_010)).thenReturn(new MessageData());

            service.getParameter("PARAM");
        });
    }

}
