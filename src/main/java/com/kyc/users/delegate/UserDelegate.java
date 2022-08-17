package com.kyc.users.delegate;

import com.kyc.core.model.jwt.TokenData;
import com.kyc.core.model.web.RequestData;
import com.kyc.core.model.web.ResponseData;
import com.kyc.users.model.CredentialData;
import com.kyc.users.service.SignInUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class UserDelegate {

    @Autowired
    private SignInUserService signInUserService;

    public ResponseEntity<ResponseData<TokenData>> signUpCustomerUser(RequestData<CredentialData> req){

        return signInUserService.signInUser(req).toResponseEntity();
    }

    public ResponseEntity<Void> sessionChecking(RequestData<Void> req){

        return null;
    }
}
