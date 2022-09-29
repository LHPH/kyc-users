package com.kyc.users.delegate;

import com.kyc.core.model.web.RequestData;
import com.kyc.core.model.web.ResponseData;
import com.kyc.users.model.CustomerData;
import com.kyc.users.service.SignUpUserService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomerUserDelegateTest {

    @Mock
    private SignUpUserService signUpUserService;

    @InjectMocks
    private CustomerUserDelegate delegate;

    @BeforeAll
    public static void init(){
        MockitoAnnotations.openMocks(CustomerUserDelegateTest.class);
    }

    @Test
    public void registerCustomerUser_passRequest_returnResponse(){

        when(signUpUserService.signUpUser(any(RequestData.class)))
                .thenReturn(ResponseData.of(true));

        delegate.registerCustomerUser(RequestData.<CustomerData>builder().build());
        verify(signUpUserService,times(1)).signUpUser(any(RequestData.class));
    }
}
