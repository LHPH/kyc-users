package com.kyc.users.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyc.core.model.web.RequestData;
import com.kyc.core.util.TestsUtil;
import com.kyc.users.delegate.CustomerUserDelegate;
import com.kyc.users.model.CustomerData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static com.kyc.users.constants.AppConstants.CHANNEL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CustomerUserController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class CustomerUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerUserDelegate delegate;

    private JacksonTester<Object> jacksonTester;

    @BeforeEach
    public void init(){

        ObjectMapper objectMapper = new ObjectMapper();
        JacksonTester.initFields(this,objectMapper);
    }

    @Test
    public void registerUser_processRequest_returnResponse() throws Exception{

        CustomerData customerData = new CustomerData();
        customerData.setPassword("PASSWORD");
        customerData.setUsername("USERNAME");
        customerData.setCustomerNumber(1L);

        String body = jacksonTester.write(customerData).getJson();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(CHANNEL,"1");

        given(delegate.registerCustomerUser(any(RequestData.class)))
                .willReturn(TestsUtil.getResponseTest(true));

        mockMvc.perform(post("/customer/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body.getBytes())
                .headers(httpHeaders))
                .andDo(print())
                .andExpect(status().isOk());
    }


}
