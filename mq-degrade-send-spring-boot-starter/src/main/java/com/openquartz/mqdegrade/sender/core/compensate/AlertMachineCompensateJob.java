package com.openquartz.mqdegrade.sender.core.compensate;

import com.openquartz.mqdegrade.sender.core.config.DegradeMessageConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class AlertMachineCompensateJob implements InitializingBean {

    private final ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1);

    private final DegradeMessageCompensateService degradeMessageCompensateService;
    private final DegradeMessageConfig degradeMessageConfig;

    public AlertMachineCompensateJob(DegradeMessageCompensateService degradeMessageCompensateService,
                                     DegradeMessageConfig degradeMessageConfig) {
        this.degradeMessageCompensateService = degradeMessageCompensateService;
        this.degradeMessageConfig = degradeMessageConfig;
    }

    @Override
    public void afterPropertiesSet() {
        scheduledExecutorService.scheduleWithFixedDelay(() -> {
                    try {
                        degradeMessageCompensateService.compensateAlert();
                    } catch (Exception ex) {
                        log.error("[AlertMachineCompensateJob#afterPropertiesSet] compensate alert error!", ex);
                    }
                },
                degradeMessageConfig.getCompensateAlertJobDelayTime(),
                degradeMessageConfig.getCompensateAlertJobPeriodTime(),
                TimeUnit.SECONDS);
    }
}
