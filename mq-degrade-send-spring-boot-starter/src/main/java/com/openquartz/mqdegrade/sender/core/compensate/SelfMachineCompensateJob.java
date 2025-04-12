package com.openquartz.mqdegrade.sender.core.compensate;

import com.openquartz.mqdegrade.sender.common.utils.IpUtils;
import com.openquartz.mqdegrade.sender.core.config.DegradeMessageConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SelfMachineCompensateJob implements InitializingBean {

    private final ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
    private final DegradeMessageCompensateService degradeMessageCompensateService;
    private final DegradeMessageConfig degradeMessageConfig;

    public SelfMachineCompensateJob(DegradeMessageCompensateService degradeMessageCompensateService,
                                    DegradeMessageConfig degradeMessageConfig) {
        this.degradeMessageCompensateService = degradeMessageCompensateService;
        this.degradeMessageConfig = degradeMessageConfig;
    }


    @Override
    public void afterPropertiesSet() {

        scheduledExecutorService.scheduleAtFixedRate(() -> {

                    String ip = IpUtils.getIp();
                    try {
                        degradeMessageCompensateService.compensate(ip);
                    } catch (Exception ex) {
                        log.error("[SelfMachineCompensateJob#SelfMachineCompensateJob,{}] compensate error!", ip, ex);
                    }
                },
                degradeMessageConfig.getCompensateSelfJobDelayTime(),
                degradeMessageConfig.getCompensateSelfJobPeriodTime(),
                TimeUnit.SECONDS);
    }
}
