package com.kyc.users.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyc.core.model.jwt.TokenData;
import com.kyc.core.model.web.RequestData;
import com.kyc.core.util.TestsUtil;
import com.kyc.users.delegate.UserDelegate;
import com.kyc.users.model.CredentialData;
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

@WebMvcTest(controllers = GeneralUserController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class GeneralUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserDelegate delegate;

    private JacksonTester<Object> jacksonTester;

    @BeforeEach
    public void init(){

        ObjectMapper objectMapper = new ObjectMapper();
        JacksonTester.initFields(this,objectMapper);
    }

    @Test
    public void loginUser_processRequest_returnResponse() throws Exception{

        CredentialData credentialData = new CredentialData();
        credentialData.setUsername("USERNAME");
        credentialData.setPassword("PASSWORD");

        String body = jacksonTester.write(credentialData).getJson();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(CHANNEL,"1");

        given(delegate.signInUser(any(RequestData.class)))
                .willReturn(TestsUtil.getResponseTest(new TokenData()));

        mockMvc.perform(post("/user/sign-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body.getBytes())
                .headers(httpHeaders))
                .andDo(print())
                .andExpect(status().isOk());

    }

    @Test
    public void sessionChecking_processRequest_returnResponse() throws Exception{

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(CHANNEL,"1");
        httpHeaders.add(HttpHeaders.AUTHORIZATION,"Bearer auth");

        given(delegate.sessionChecking(any(RequestData.class)))
                .willReturn(TestsUtil.getResponseTest(null));

        mockMvc.perform(post("/user/session-checking")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders))
                .andDo(print())
                .andExpect(status().isOk());

    }

    @Test
    public void logoutUser_processRequest_returnResponse() throws Exception{

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(CHANNEL,"1");
        httpHeaders.add(HttpHeaders.AUTHORIZATION,"Bearer auth");

        given(delegate.signOutUser(any(RequestData.class)))
                .willReturn(TestsUtil.getResponseTest(null));

        mockMvc.perform(post("/user/sign-out")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
