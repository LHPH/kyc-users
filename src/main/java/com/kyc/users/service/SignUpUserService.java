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
import org.passay.PasswordData;
import org.passay.RuleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.kyc.users.constants.AppConstants.MSG_APP_003;
import static com.kyc.users.constants.AppConstants.MSG_APP_004;
import static com.kyc.users.constants.AppConstants.MSG_APP_005;
import static com.kyc.users.constants.AppConstants.MSG_APP_010;

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
    private KycMessages kycMessages;

    public ResponseData<Boolean> signUpUser(RequestData<CustomerData> req){

        CustomerData customerData = req.getBody();

        verifiedPassword(customerData);

        LOGGER.info("Checking if the username already exists en database {}", customerData.getUsername());
        Optional<KycUser> opUser = kycUserRepository.findByUsername(customerData.getUsername());
        if(!opUser.isPresent()){

            if(!verifiedIfCustomerHasAlreadyUser(customerData.getCustomerNumber())){

                saveUserDatabase(customerData);
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
        LOGGER.info("Checking if the password meets the password policy for {}",req.getUsername());
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

    private void saveUserDatabase(CustomerData req){

        try{

            KycUser entity = customerUserMapper.toEntityForSigningUp(req);

            Optional<KycUserType> opUserType = kycUserTypeRepository.findById(KycUserTypeEnum.CUSTOMER.getId());
            if(opUserType.isPresent()){

                KycUserRelation relation = new KycUserRelation();
                relation.setIdCustomer(req.getCustomerNumber());
                relation.setUser(entity);
                relation.setUserType(opUserType.get());

                entity.setUserRelation(relation);

                LOGGER.info("Saving the user data in the database {}", req.getUsername());
                kycUserRepository.save(entity);
                LOGGER.info("The user data was saved in database {}", req.getUsername());
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
