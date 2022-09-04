package com.kyc.users.delegate;

import com.kyc.core.model.jwt.TokenData;
import com.kyc.core.model.web.RequestData;
import com.kyc.core.model.web.ResponseData;
import com.kyc.users.model.CredentialData;
import com.kyc.users.service.UserAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class UserDelegate {

    @Autowired
    private UserAuthService userAuthService;

    public ResponseEntity<ResponseData<TokenData>> signInUser(RequestData<CredentialData> req){

        return userAuthService.signInUser(req).toResponseEntity();
    }

    public ResponseEntity<ResponseData<Void>> signOutUser(RequestData<Void> req){

        return userAuthService.signOutUser(req).toResponseEntity();
    }

    public ResponseEntity<ResponseData<Void>> sessionChecking(RequestData<Void> req){

        return userAuthService.renewSession(req).toResponseEntity();
    }
}
