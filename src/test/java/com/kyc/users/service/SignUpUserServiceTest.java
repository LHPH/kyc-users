package com.kyc.users.service;

import com.kyc.core.exception.KycRestException;
import com.kyc.core.model.MessageData;
import com.kyc.core.model.web.RequestData;
import com.kyc.core.persistence.entity.KycUserType;
import com.kyc.core.persistence.repositories.KycUserTypeRepository;
import com.kyc.core.properties.KycMessages;
import com.kyc.core.services.PasswordFormatValidationService;
import com.kyc.users.entity.KycUserExtend;
import com.kyc.users.enums.KycUserTypeEnum;
import com.kyc.users.mappers.CustomerUserMapper;
import com.kyc.users.model.CustomerData;
import com.kyc.users.repositories.KycCustomerRepository;
import com.kyc.users.repositories.KycUserExtendRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.passay.FailureValidationResult;
import org.passay.PasswordData;
import org.passay.SuccessValidationResult;
import org.passay.ValidationResult;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.Optional;

import static com.kyc.core.constants.GeneralConstants.CHANNEL;
import static com.kyc.users.constants.AppConstants.MSG_APP_004;
import static com.kyc.users.constants.AppConstants.MSG_APP_005;
import static com.kyc.users.constants.AppConstants.MSG_APP_010;
import static com.kyc.users.constants.AppConstants.MSG_APP_012;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SignUpUserServiceTest {

    @Mock
    private KycUserExtendRepository kycUserRepository;

    @Mock
    private KycUserTypeRepository kycUserTypeRepository;

    @Mock
    private KycCustomerRepository kycCustomerRepository;

    @Mock
    private PasswordFormatValidationService passwordFormatValidationService;

    @Mock
    private CustomerUserMapper customerUserMapper;

    @Mock
    private NotificationService notificationService;

    @Mock
    private KycMessages kycMessages;

    @InjectMocks
    private SignUpUserService service;

    @BeforeAll
    public static void setUp(){
        MockitoAnnotations.openMocks(SignUpUserServiceTest.class);
    }

    @Test
    public void signUpUser_registerNewUser_successfulRegistration(){

        ValidationResult ruleResult = new SuccessValidationResult();

        KycUserExtend kycUser = new KycUserExtend();
        kycUser.setId(1L);

        when(kycUserRepository.findByUsername(anyString()))
                .thenReturn(Optional.empty());
        when(passwordFormatValidationService.validatePassword(any(PasswordData.class)))
                .thenReturn(ruleResult);
        when(kycCustomerRepository.countHaveUser(anyLong()))
                .thenReturn(0L);
        when(customerUserMapper.toEntityForSigningUp(any(CustomerData.class)))
                .thenReturn(kycUser);
        when(kycUserTypeRepository.findById(KycUserTypeEnum.CUSTOMER.getId()))
                .thenReturn(Optional.of(new KycUserType()));
        when(kycUserRepository.save(any(KycUserExtend.class)))
                .thenReturn(kycUser);
        when(kycMessages.getMessage(MSG_APP_012))
                .thenReturn(new MessageData());

        RequestData<CustomerData> req = RequestData.<CustomerData>builder()
                .headers(Collections.singletonMap(CHANNEL,"1"))
                .body(new CustomerData("TEST","TEST",1L))
                .build();

        service.signUpUser(req);
        verify(kycUserRepository,times(1)).save(any(KycUserExtend.class));
        verify(notificationService,times(1)).sendNotificationTo(anyInt(),anyLong(),any(MessageData.class));
    }

    @Test
    public void signUpUser_registerExistingUsername_noRegistration(){

        KycRestException ex = Assertions.assertThrows(KycRestException.class,()->{

            ValidationResult ruleResult = new SuccessValidationResult();

            KycUserExtend kycUser = new KycUserExtend();
            kycUser.setId(1L);

            when(kycUserRepository.findByUsername(anyString()))
                    .thenReturn(Optional.ofNullable(kycUser));
            when(passwordFormatValidationService.validatePassword(any(PasswordData.class)))
                    .thenReturn(ruleResult);

            when(kycMessages.getMessage(MSG_APP_005))
                    .thenReturn(new MessageData());

            RequestData<CustomerData> req = RequestData.<CustomerData>builder()
                    .headers(Collections.singletonMap(CHANNEL,"1"))
                    .body(new CustomerData("TEST","TEST",1L))
                    .build();

            service.signUpUser(req);
        });
        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY,ex.getStatus());
    }

    @Test
    public void signUpUser_registerNewUserButPasswordInvalid_noRegistration(){

        KycRestException ex = Assertions.assertThrows(KycRestException.class,()->{

            ValidationResult ruleResult = new FailureValidationResult();

            when(passwordFormatValidationService.validatePassword(any(PasswordData.class)))
                    .thenReturn(ruleResult);

            when(kycMessages.getMessage(MSG_APP_004))
                    .thenReturn(new MessageData());

            RequestData<CustomerData> req = RequestData.<CustomerData>builder()
                    .headers(Collections.singletonMap(CHANNEL,"1"))
                    .body(new CustomerData("TEST","TEST",1L))
                    .build();

            service.signUpUser(req);
        });
        Assertions.assertEquals(HttpStatus.BAD_REQUEST,ex.getStatus());
    }

    @Test
    public void signUpUser_registerCustomerWithAlreadyUser_noRegistration(){

        KycRestException ex = Assertions.assertThrows(KycRestException.class,()->{

            ValidationResult ruleResult = new SuccessValidationResult();

            when(kycUserRepository.findByUsername(anyString()))
                    .thenReturn(Optional.empty());
            when(passwordFormatValidationService.validatePassword(any(PasswordData.class)))
                    .thenReturn(ruleResult);
            when(kycCustomerRepository.countHaveUser(anyLong()))
                    .thenReturn(1L);

            when(kycMessages.getMessage(MSG_APP_005))
                    .thenReturn(new MessageData());

            RequestData<CustomerData> req = RequestData.<CustomerData>builder()
                    .headers(Collections.singletonMap(CHANNEL,"1"))
                    .body(new CustomerData("TEST","TEST",1L))
                    .build();

            service.signUpUser(req);
        });
        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY,ex.getStatus());
    }

    @Test
    public void signUpUser_registerNewUserButUserTypeNonExistent_noRegistration(){

        KycRestException ex = Assertions.assertThrows(KycRestException.class,()->{

            ValidationResult ruleResult = new SuccessValidationResult();

            KycUserExtend kycUser = new KycUserExtend();
            kycUser.setId(1L);

            when(kycUserRepository.findByUsername(anyString()))
                    .thenReturn(Optional.empty());
            when(passwordFormatValidationService.validatePassword(any(PasswordData.class)))
                    .thenReturn(ruleResult);
            when(kycCustomerRepository.countHaveUser(anyLong()))
                    .thenReturn(0L);
            when(customerUserMapper.toEntityForSigningUp(any(CustomerData.class)))
                    .thenReturn(kycUser);
            when(kycUserTypeRepository.findById(KycUserTypeEnum.CUSTOMER.getId()))
                    .thenReturn(Optional.empty());

            when(kycMessages.getMessage(MSG_APP_010))
                    .thenReturn(new MessageData());

            RequestData<CustomerData> req = RequestData.<CustomerData>builder()
                    .headers(Collections.singletonMap(CHANNEL,"1"))
                    .body(new CustomerData("TEST","TEST",1L))
                    .build();

            service.signUpUser(req);
        });
        Assertions.assertEquals(HttpStatus.SERVICE_UNAVAILABLE,ex.getStatus());
    }
}
