package com.kyc.users.controllers;

import com.kyc.core.model.web.RequestData;
import com.kyc.core.model.web.ResponseData;
import com.kyc.users.delegate.CustomerUserDelegate;
import com.kyc.users.model.CustomerData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import java.util.HashMap;
import java.util.Map;

import static com.kyc.users.constants.AppConstants.CHANNEL;

@RestController
@Validated
@RequestMapping("/customer")
public class CustomerUserController {

    @Autowired
    private CustomerUserDelegate customerUserDelegate;

    @PostMapping("/sign-up")
    public ResponseEntity<ResponseData<Boolean>> registerUser(@RequestHeader(CHANNEL) Integer channel,
                                                              @Valid @RequestBody CustomerData req){

        Map<String,Object> headers = new HashMap<>();
        headers.put(CHANNEL,channel);

        RequestData<CustomerData> input = RequestData.<CustomerData>builder()
                .headers(headers)
                .body(req)
                .build();

        return customerUserDelegate.registerCustomerUser(input);
    }


}
