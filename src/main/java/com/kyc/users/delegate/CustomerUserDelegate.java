package com.kyc.users.delegate;

import com.kyc.core.model.web.RequestData;
import com.kyc.core.model.web.ResponseData;
import com.kyc.users.model.CredentialData;
import com.kyc.users.model.CustomerData;
import com.kyc.users.service.SignInUserService;
import com.kyc.users.service.SignUpUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class CustomerUserDelegate {

    @Autowired
    private SignUpUserService signUpUserService;

    public ResponseEntity<ResponseData<Boolean>> registerCustomerUser(RequestData<CustomerData> req){

        return signUpUserService.signUpUser(req).toResponseEntity();
    }


}
