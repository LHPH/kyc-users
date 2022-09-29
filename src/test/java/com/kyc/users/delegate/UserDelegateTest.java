package com.kyc.users.delegate;

import com.kyc.core.model.jwt.TokenData;
import com.kyc.core.model.web.RequestData;
import com.kyc.core.model.web.ResponseData;
import com.kyc.users.model.CredentialData;
import com.kyc.users.service.UserAuthService;
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
public class UserDelegateTest {

    @Mock
    private UserAuthService userAuthService;

    @InjectMocks
    private UserDelegate delegate;

    @BeforeAll
    public static void init(){
        MockitoAnnotations.openMocks(UserDelegateTest.class);
    }

    @Test
    public void signInUser_passRequest_returnResponse(){

        when(userAuthService.signInUser(any(RequestData.class)))
                .thenReturn(ResponseData.of(new TokenData()));

        delegate.signInUser(RequestData.<CredentialData>builder().build());

        verify(userAuthService,times(1)).signInUser(any(RequestData.class));
    }

    @Test
    public void signOutUser_passRequest_returnResponse(){

        when(userAuthService.signOutUser(any(RequestData.class)))
                .thenReturn(ResponseData.of(null));

        delegate.signOutUser(RequestData.<Void>builder().build());

        verify(userAuthService,times(1)).signOutUser(any(RequestData.class));
    }

    @Test
    public void sessionChecking_passRequest_returnResponse(){

        when(userAuthService.renewSession(any(RequestData.class)))
                .thenReturn(ResponseData.of(null));

        delegate.sessionChecking(RequestData.<Void>builder().build());

        verify(userAuthService,times(1)).renewSession(any(RequestData.class));
    }


}
