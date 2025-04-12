package com.openquartz.mqdegrade.sender.core.compensate;

import com.openquartz.mqdegrade.sender.core.config.DegradeMessageConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 全局机器补偿任务
 *
 * @author svnee
 */
@Slf4j
public class GlobalMachineCompensateJob implements InitializingBean {

    private final ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
    private final DegradeMessageCompensateService degradeMessageCompensateService;
    private final DegradeMessageConfig degradeMessageConfig;

    public GlobalMachineCompensateJob(DegradeMessageCompensateService degradeMessageCompensateService,
                                      DegradeMessageConfig degradeMessageConfig) {
        this.degradeMessageCompensateService = degradeMessageCompensateService;
        this.degradeMessageConfig = degradeMessageConfig;
    }

    @Override
    public void afterPropertiesSet() {

        scheduledExecutorService.scheduleAtFixedRate(() -> {
                    try {
                        degradeMessageCompensateService.compensate();
                    } catch (Exception ex) {
                        log.error("[GlobalMachineCompensateJob#afterPropertiesSet] compensate-error!", ex);
                    }
                },
                degradeMessageConfig.getCompensateGlobalJobDelayTime(),
                degradeMessageConfig.getCompensateGlobalJobPeriodTime(),
                TimeUnit.SECONDS);
    }
}
