package com.kyc.users.service;

import com.kyc.core.enums.MessageType;
import com.kyc.core.model.MessageData;
import com.kyc.core.model.notifications.NotificationData;
import com.kyc.users.entity.KycUserExtend;
import com.kyc.users.repositories.KycUserExtendRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.dao.InvalidDataAccessResourceUsageException;

import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import static com.kyc.users.constants.AppConstants.KYC_USERS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private KycUserExtendRepository kycUserRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private Clock clock;

    @InjectMocks
    private NotificationService notificationService;

    private static final ZonedDateTime NOW = ZonedDateTime.of(
            2022,
            9,
            10,
            10,
            10,
            10,
            0,
            ZoneId.systemDefault()
    );

    @BeforeAll
    public static void init(){
        MockitoAnnotations.openMocks(NotificationServiceTest.class);
    }

    @Test
    public void sendNotificationTo_sendingNotification_sentNotification(){

        KycUserExtend user = new KycUserExtend();
        user.setId(3L);
        when(kycUserRepository.findByUsernameAndActiveTrue(KYC_USERS))
                .thenReturn(Optional.of(user));

        notificationService.sendNotificationTo(1,1L,new MessageData("CODE","MESSAGE", MessageType.INFO));
        verify(rabbitTemplate,times(1))
                .convertAndSend(anyString(),anyString(),any(NotificationData.class),any(MessagePostProcessor.class));
    }

    @Test
    public void sendNotificationTo_userNotFound_notificationNotSent(){

        when(kycUserRepository.findByUsernameAndActiveTrue(KYC_USERS))
                .thenReturn(Optional.empty());

        notificationService.sendNotificationTo(1,1L,new MessageData("CODE","MESSAGE", MessageType.INFO));
        verify(rabbitTemplate,times(0))
                .convertAndSend(anyString(),anyString(),any(NotificationData.class),any(MessagePostProcessor.class));
    }

    @Test
    public void sendNotificationTo_errorSendingNotificationByRabbit_notificationNotSent(){

        KycUserExtend user = new KycUserExtend();
        user.setId(3L);
        when(kycUserRepository.findByUsernameAndActiveTrue(KYC_USERS))
                .thenReturn(Optional.of(user));
        doThrow(new AmqpException("amqp error")).when(rabbitTemplate)
                .convertAndSend(anyString(),anyString(),any(NotificationData.class),any(MessagePostProcessor.class));

        notificationService.sendNotificationTo(1,1L,new MessageData("CODE","MESSAGE", MessageType.INFO));
        verify(rabbitTemplate,times(1))
                .convertAndSend(anyString(),anyString(),any(NotificationData.class),any(MessagePostProcessor.class));
    }

    @Test
    public void sendNotificationTo_unavailableDatabase_notificationNotSent(){

        KycUserExtend user = new KycUserExtend();
        user.setId(3L);
        when(kycUserRepository.findByUsernameAndActiveTrue(KYC_USERS))
                .thenThrow(new InvalidDataAccessResourceUsageException("test db error"));

        notificationService.sendNotificationTo(1,1L,new MessageData("CODE","MESSAGE", MessageType.INFO));
        verify(rabbitTemplate,times(0))
                .convertAndSend(anyString(),anyString(),any(NotificationData.class),any(MessagePostProcessor.class));
    }
}
