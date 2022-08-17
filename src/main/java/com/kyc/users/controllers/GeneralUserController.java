package com.kyc.users.controllers;

import com.kyc.core.model.jwt.TokenData;
import com.kyc.core.model.web.RequestData;
import com.kyc.core.model.web.ResponseData;
import com.kyc.users.delegate.UserDelegate;
import com.kyc.users.model.CredentialData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.kyc.users.constants.AppConstants.CHANNEL;
import static com.kyc.users.constants.AppConstants.IP;

@RestController
@RequestMapping("/user")
public class GeneralUserController {

    @Autowired
    private UserDelegate userDelegate;

    @PostMapping("/sign-in")
    public ResponseEntity<ResponseData<TokenData>> loginUser(@RequestHeader(CHANNEL) String channel,
                                                             @Valid @RequestBody CredentialData req){
        Map<String,Object> headers = new HashMap<>();
        headers.put(CHANNEL,channel);
        headers.put(IP,"192.168.0.0");

        RequestData<CredentialData> input = RequestData.<CredentialData>builder()
                .headers(headers)
                .body(req)
                .build();
        return userDelegate.signUpCustomerUser(input);
    }

    @GetMapping("/session-checking")
    public ResponseEntity<Void> sessionChecking(@RequestHeader(HttpHeaders.AUTHORIZATION) String auth){

        RequestData<Void> req = RequestData.<Void>builder()
                .headers(Collections.singletonMap(HttpHeaders.AUTHORIZATION,auth))
                .build();

        return null;
    }
}
