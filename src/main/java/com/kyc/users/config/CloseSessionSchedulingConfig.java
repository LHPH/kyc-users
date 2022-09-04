package com.kyc.users.config;

import com.kyc.core.exception.KycRestException;
import com.kyc.users.entity.KycParameter;
import com.kyc.users.service.ParameterService;
import com.kyc.users.service.SessionService;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

import static com.kyc.users.constants.AppConstants.KYC_SESSION_TIMEOUT;


@Configuration
@EnableScheduling
public class CloseSessionSchedulingConfig implements SchedulingConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(CloseSessionSchedulingConfig.class);

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private SessionService sessionService;

    @Bean
    public TaskScheduler  taskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(5);
        threadPoolTaskScheduler.setThreadNamePrefix("AutoCloseSession-");
        return threadPoolTaskScheduler;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {

        KycParameter parameter = parameterService.getParameter(KYC_SESSION_TIMEOUT);

        taskRegistrar.setScheduler(taskScheduler());
        taskRegistrar.addTriggerTask(() -> {
            try {
                sessionService.closeIdleActiveSessions();
            } catch (KycRestException ex) {
                LOGGER.error(" ",ex);
            }
        }, context -> {

            Optional<Date> lastCompletionTime =
                    Optional.ofNullable(context.lastCompletionTime());
            Instant nextExecutionTime =
                    lastCompletionTime.orElseGet(Date::new).toInstant()
                            .plus(NumberUtils.toLong(parameter.getValue()), ChronoUnit.MINUTES);
            return Date.from(nextExecutionTime);
        });
    }
}
