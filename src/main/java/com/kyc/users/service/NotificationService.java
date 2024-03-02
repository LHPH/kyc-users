package com.kyc.users.service;

import com.kyc.core.model.jwt.JWTData;
import com.kyc.core.model.notifications.NotificationData;
import com.kyc.core.model.MessageData;
import com.kyc.core.util.DateUtil;
import com.kyc.users.entity.KycUser;
import com.kyc.users.repositories.KycUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.kyc.users.constants.AppConstants.CHANNEL;
import static com.kyc.users.constants.AppConstants.KYC_USERS;

@Service
public class NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private KycUserRepository kycUserRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private Clock clock;

    @Value("${kyc-config.token.audience.notifications}")
    private String tokenAudience;

    private static final String EXCHANGE_CUSTOMERS = "kyc.customers";
    private static final String ROUTING_KEY = "kyc.customers.user";

    public void sendNotificationTo(Integer idChannel,Long customerId,MessageData messageData){

        try{

            Optional<KycUser> opUser = kycUserRepository.findByUsernameAndActiveTrue(KYC_USERS);
            if(opUser.isPresent()){

                KycUser user = opUser.get();

                JWTData jwtData = new JWTData();
                jwtData.setChannel(String.valueOf(idChannel));
                jwtData.setIssuer(KYC_USERS);
                jwtData.setSubject(String.valueOf(user.getId()));
                jwtData.setAudience(tokenAudience);
                jwtData.setExpirationTime(DateUtil.localDateTimeToDate(LocalDateTime.now(clock).plusMinutes(5)));

                String token = tokenService.getToken(jwtData);

                Map<String,Object> headers = new HashMap<>();
                headers.put("Authorization",token);
                headers.put("kyc-customer-id-receiver", customerId);
                headers.put(CHANNEL,idChannel);

                NotificationData notificationData = new NotificationData();
                notificationData.setMessage(messageData.getMessage());
                notificationData.setEvent(messageData.getType().name());
                notificationData.setDate(new Date());
                LOGGER.info("Send Notification");
                rabbitTemplate.convertAndSend(EXCHANGE_CUSTOMERS,ROUTING_KEY,notificationData,m ->{

                    m.getMessageProperties().getHeaders().putAll(headers);
                    return m;
                });

            }
            else{
                LOGGER.warn("It could not send notification due the system user did not found or is inactive");
            }
        }
        catch(AmqpException ex){
            LOGGER.error("It could not send notification due an amqp error ",ex);
        }
        catch(DataAccessException ex){
            LOGGER.error("It could not send notification due an database error ",ex);
        }
    }
}
