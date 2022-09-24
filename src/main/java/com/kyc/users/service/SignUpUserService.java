package com.kyc.users.service;

import com.kyc.core.exception.KycRestException;
import com.kyc.core.model.web.RequestData;
import com.kyc.core.model.web.ResponseData;
import com.kyc.core.properties.KycMessages;
import com.kyc.core.services.PasswordFormatValidationService;
import com.kyc.users.entity.KycUser;
import com.kyc.users.entity.KycUserRelation;
import com.kyc.users.entity.KycUserType;
import com.kyc.users.enums.KycUserTypeEnum;
import com.kyc.users.mappers.CustomerUserMapper;
import com.kyc.users.model.CustomerData;
import com.kyc.users.repositories.KycUserRelationRepository;
import com.kyc.users.repositories.KycUserRepository;
import com.kyc.users.repositories.KycUserTypeRepository;
import org.apache.commons.lang3.math.NumberUtils;
import org.passay.PasswordData;
import org.passay.RuleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

import static com.kyc.users.constants.AppConstants.CHANNEL;
import static com.kyc.users.constants.AppConstants.MSG_APP_004;
import static com.kyc.users.constants.AppConstants.MSG_APP_005;
import static com.kyc.users.constants.AppConstants.MSG_APP_010;
import static com.kyc.users.constants.AppConstants.MSG_APP_012;

@Service
public class SignUpUserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SignUpUserService.class);

    @Autowired
    private KycUserRepository kycUserRepository;

    @Autowired
    private KycUserTypeRepository kycUserTypeRepository;

    @Autowired
    private KycUserRelationRepository kycUserRelationRepository;

    @Autowired
    private PasswordFormatValidationService passwordFormatValidationService;

    @Autowired
    private CustomerUserMapper customerUserMapper;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private KycMessages kycMessages;

    public ResponseData<Boolean> signUpUser(RequestData<CustomerData> req){

        LOGGER.info("Process to register a new user");
        CustomerData customerData = req.getBody();
        Map<String,Object> headers = req.getHeaders();
        Integer idChannel = NumberUtils.toInt(headers.get(CHANNEL).toString());
        LOGGER.info("The channel where the process is doing is {}",idChannel);

        verifiedPassword(customerData);
        LOGGER.info("The password meets the requirements");

        LOGGER.info("Checking if the username already exists en database");
        Optional<KycUser> opUser = kycUserRepository.findByUsername(customerData.getUsername());
        if(!opUser.isPresent()){

            LOGGER.info("Checking if the user already has a user");
            if(!verifiedIfCustomerHasAlreadyUser(customerData.getCustomerNumber())){

                LOGGER.info("Saving the new user in database");
                Long idNewUser = saveUserDatabase(customerData);
                LOGGER.info("The id of the created user is {}",idNewUser);
                notificationService.sendNotificationTo(idChannel,idNewUser,kycMessages.getMessage(MSG_APP_012));
                LOGGER.info("Finish process to register a new user");
                return ResponseData.of(true);
            }
            throw KycRestException.builderRestException()
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .errorData(kycMessages.getMessage(MSG_APP_005))
                    .inputData(req)
                    .build();
        }
        throw KycRestException.builderRestException()
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .errorData(kycMessages.getMessage(MSG_APP_005))
                .inputData(req)
                .build();
    }

    private void verifiedPassword(CustomerData req){

        PasswordData passwordData = new PasswordData(req.getUsername(),req.getPassword());
        LOGGER.info("Checking if the password meets the password policy");
        RuleResult result = passwordFormatValidationService.validatePassword(passwordData);

        if(!result.isValid()){
            throw KycRestException.builderRestException()
                    .status(HttpStatus.BAD_REQUEST)
                    .errorData(kycMessages.getMessage(MSG_APP_004))
                    .inputData(req)
                    .build();
        }
    }

    public boolean verifiedIfCustomerHasAlreadyUser(Long customerNumber){

        Optional<KycUserRelation> opUserRelation = kycUserRelationRepository.findByIdCustomer(customerNumber);
        return opUserRelation.isPresent();
    }

    private Long saveUserDatabase(CustomerData req){

        try{

            KycUser entity = customerUserMapper.toEntityForSigningUp(req);

            Optional<KycUserType> opUserType = kycUserTypeRepository.findById(KycUserTypeEnum.CUSTOMER.getId());
            if(opUserType.isPresent()){

                KycUserRelation relation = new KycUserRelation();
                relation.setIdCustomer(req.getCustomerNumber());
                relation.setUser(entity);
                relation.setUserType(opUserType.get());

                entity.setUserRelation(relation);

                LOGGER.info("Saving the user data in the database");
                KycUser result = kycUserRepository.save(entity);
                LOGGER.info("The user data was saved in database");
                return result.getId();
            }
            else{
                throw KycRestException.builderRestException()
                        .status(HttpStatus.SERVICE_UNAVAILABLE)
                        .errorData(kycMessages.getMessage(MSG_APP_010))
                        .inputData(req)
                        .build();
            }
        }
        catch(DataAccessException ex){

            throw KycRestException.builderRestException()
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .errorData(kycMessages.getMessage(MSG_APP_010))
                    .exception(ex)
                    .inputData(req)
                    .build();
        }
    }


}
