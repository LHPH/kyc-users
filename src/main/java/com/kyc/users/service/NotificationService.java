package com.kyc.users.service;

import com.kyc.core.model.MessageData;
import com.kyc.core.model.jwt.JwtData;
import com.kyc.core.model.notifications.NotificationData;
import com.kyc.core.util.DateUtil;
import com.kyc.users.entity.KycUserExtend;
import com.kyc.users.repositories.KycUserExtendRepository;
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

import static com.kyc.core.constants.GeneralConstants.CHANNEL;
import static com.kyc.core.constants.GeneralConstants.ID_ISSUER;
import static com.kyc.core.constants.GeneralConstants.ID_RECIPIENT;
import static com.kyc.users.constants.AppConstants.KYC_USERS;

@Service
public class NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private KycUserExtendRepository kycUserRepository;

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

            Optional<KycUserExtend> opUser = kycUserRepository.findByUsernameAndActiveTrue(KYC_USERS);
            if(opUser.isPresent()){

                KycUserExtend user = opUser.get();

                Map<String,Object> headers = new HashMap<>();
                headers.put(ID_ISSUER,user.getId());
                headers.put(ID_RECIPIENT, customerId);
                headers.put(CHANNEL,idChannel);

                NotificationData notificationData = new NotificationData();
                notificationData.setMessage(messageData.getMessage());
                notificationData.setEvent(messageData.getType().name());
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
