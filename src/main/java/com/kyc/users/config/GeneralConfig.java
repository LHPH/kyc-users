package com.kyc.users.config;

import com.kyc.core.config.BuildDetailConfig;
import com.kyc.core.exception.handlers.KycGenericRestExceptionHandler;
import com.kyc.core.exception.handlers.KycUnhandledExceptionHandler;
import com.kyc.core.exception.handlers.KycValidationRestExceptionHandler;
import com.kyc.core.properties.KycMessages;
import com.kyc.core.services.PasswordEncoderService;
import com.kyc.core.services.PasswordFormatValidationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.time.Clock;
import java.time.ZoneId;

import static com.kyc.users.constants.AppConstants.MSG_APP_001;
import static com.kyc.users.constants.AppConstants.MSG_APP_002;

@Configuration
@Import(value = {KycMessages.class, KycGenericRestExceptionHandler.class, BuildDetailConfig.class})
public class GeneralConfig {

    @Bean
    public KycUnhandledExceptionHandler kycUnhandledExceptionHandler(KycMessages kycMessages){

        return new KycUnhandledExceptionHandler(kycMessages.getMessage(MSG_APP_001));
    }

    @Bean
    public KycValidationRestExceptionHandler kycValidationRestExceptionHandler(KycMessages kycMessages){

        return new KycValidationRestExceptionHandler(kycMessages.getMessage(MSG_APP_002));
    }

    @Bean
    public PasswordFormatValidationService passwordFormatValidationService(){
        return new PasswordFormatValidationService();
    }

    @Bean
    public PasswordEncoderService passwordEncoderService(){
        return new PasswordEncoderService();
    }

    @Bean
    public Clock clock(){
        return Clock.system(ZoneId.of("UTC-6"));
    }

}
