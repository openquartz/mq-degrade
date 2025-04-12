package com.openquartz.mqdegrade.sender.starter.autoconfig;

import com.openquartz.mqdegrade.sender.common.TransactionProxy;
import com.openquartz.mqdegrade.sender.common.alert.DefaultDegradeAlertImpl;
import com.openquartz.mqdegrade.sender.common.alert.DegradeAlert;
import com.openquartz.mqdegrade.sender.core.compensate.AlertMachineCompensateJob;
import com.openquartz.mqdegrade.sender.core.compensate.DegradeMessageCompensateService;
import com.openquartz.mqdegrade.sender.core.compensate.GlobalMachineCompensateJob;
import com.openquartz.mqdegrade.sender.core.compensate.SelfMachineCompensateJob;
import com.openquartz.mqdegrade.sender.core.compensate.impl.DegradeMessageCompensateServiceImpl;
import com.openquartz.mqdegrade.sender.core.config.DegradeMessageConfig;
import com.openquartz.mqdegrade.sender.core.context.DefaultThreadContextSerializer;
import com.openquartz.mqdegrade.sender.core.context.ThreadContextSerializer;
import com.openquartz.mqdegrade.sender.core.send.DegradeMessageFilter;
import com.openquartz.mqdegrade.sender.core.send.SendMessageFacade;
import com.openquartz.mqdegrade.sender.core.send.impl.SendMessageFacadeImpl;
import com.openquartz.mqdegrade.sender.persist.mapper.DegradeMessageEntityMapper;
import com.openquartz.mqdegrade.sender.persist.service.DegradeMessageStorageService;
import com.openquartz.mqdegrade.sender.persist.service.impl.DegradeMessageStorageServiceImpl;
import com.openquartz.mqdegrade.sender.starter.autoconfig.impl.DefaultDegradeMessageFilterImpl;
import com.openquartz.mqdegrade.sender.starter.autoconfig.impl.DegradeMessageConfigImpl;
import com.openquartz.mqdegrade.sender.starter.autoconfig.property.*;
import com.openquartz.mqdegrade.sender.starter.autoconfig.transaction.DefaultTransactionProxyImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author svnee
 */
@ConditionalOnProperty(name = "mq.degrade.common.enable", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties({
        DegradeCommonProperty.class,
        DegradeCompensateProperty.class,
        DegradeConfigProperty.class,
        DegradeFilterProperty.class,
        DegradeParallelTransferProperty.class
})
public class DegradeConfiguration {

    @Bean("degradeMessageConfig")
    public DegradeMessageConfig degradeMessageConfig(DegradeCommonProperty degradeCommonProperty,
                                                     DegradeParallelTransferProperty degradeParallelTransferProperty,
                                                     DegradeCompensateProperty degradeCompensateProperty,
                                                     @Qualifier("parallelDegradeThreadPoolExecutor") Executor parallelDegradeThreadPoolExecutor) {
        return new DegradeMessageConfigImpl(degradeCommonProperty, degradeCompensateProperty, degradeParallelTransferProperty, parallelDegradeThreadPoolExecutor);
    }

    @Bean(name = "parallelDegradeThreadPoolExecutor")
    @ConditionalOnMissingBean(name = "parallelDegradeThreadPoolExecutor")
    public Executor parallelDegradeThreadPoolExecutor(DegradeParallelTransferProperty degradeParallelTransferProperty) {

        DegradeParallelTransferProperty.DegradeParallelTransferThreadPoolProperty threadPoolProperty = degradeParallelTransferProperty.getThreadPool();
        return new ThreadPoolExecutor(threadPoolProperty.getCorePoolSize(),
                threadPoolProperty.getMaxPoolSize(),
                threadPoolProperty.getKeepAliveTime(),
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(threadPoolProperty.getQueueCapacity()));
    }

    @Bean
    @ConditionalOnMissingBean
    public TransactionProxy transactionProxy() {
        return new DefaultTransactionProxyImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public ThreadContextSerializer threadContextSerializer() {
        return new DefaultThreadContextSerializer();
    }

    @Bean
    @ConditionalOnMissingBean
    public DegradeMessageEntityMapper degradeMessageEntityMapper(@Qualifier("degradeMessageJdbcTemplate") JdbcTemplate jdbcTemplate) {
        return new DegradeMessageEntityMapper(jdbcTemplate);
    }

    @Bean
    @Primary
    public SendMessageFacade sendMessageFacade(DegradeMessageConfig degradeMessageConfig,
                                               DegradeMessageStorageService degradeMessageStorageService,
                                               DegradeMessageFilter degradeMessageFilter) {
        return new SendMessageFacadeImpl(degradeMessageConfig, degradeMessageStorageService, degradeMessageFilter);
    }

    @Bean
    public DegradeMessageStorageService degradeMessageStorageService(DegradeMessageEntityMapper degradeMessageEntityMapper,
                                                                     ThreadContextSerializer threadContextSerializer,
                                                                     TransactionProxy transactionProxy) {
        return new DegradeMessageStorageServiceImpl(degradeMessageEntityMapper, threadContextSerializer, transactionProxy);
    }

    @Bean
    @ConditionalOnMissingBean
    public DegradeMessageFilter degradeMessageFilter(DegradeFilterProperty degradeFilterProperty) {
        return new DefaultDegradeMessageFilterImpl(degradeFilterProperty);
    }

    @Bean
    @ConditionalOnMissingBean
    public DegradeAlert degradeAlert() {
        return new DefaultDegradeAlertImpl();
    }

    @Bean
    public DegradeMessageCompensateService degradeMessageCompensateService(DegradeMessageConfig degradeMessageConfig,
                                                                           DegradeMessageStorageService degradeMessageStorageService,
                                                                           ThreadContextSerializer threadContextSerializer,
                                                                           DegradeAlert degradeAlert) {
        return new DegradeMessageCompensateServiceImpl(degradeMessageConfig, degradeMessageStorageService, threadContextSerializer, degradeAlert);
    }

    @Bean
    @ConditionalOnProperty(name = "mq.degrade.compensate.enable", havingValue = "true", matchIfMissing = true)
    public GlobalMachineCompensateJob globalMachineCompensateJob(DegradeMessageCompensateService degradeMessageCompensateService,
                                                                 DegradeMessageConfig degradeMessageConfig) {
        return new GlobalMachineCompensateJob(degradeMessageCompensateService, degradeMessageConfig);
    }

    @Bean
    @ConditionalOnProperty(name = "mq.degrade.compensate.enable", havingValue = "true", matchIfMissing = true)
    public SelfMachineCompensateJob selfMachineCompensateJob(DegradeMessageCompensateService degradeMessageCompensateService,
                                                             DegradeMessageConfig degradeMessageConfig) {
        return new SelfMachineCompensateJob(degradeMessageCompensateService, degradeMessageConfig);
    }

    @Bean
    @ConditionalOnExpression("${mq.degrade.compensate.enable:false} && ${mq.degrade.compensate.alert-enable:true}")
    public AlertMachineCompensateJob alertMachineCompensateJob(DegradeMessageCompensateService degradeMessageCompensateService,
                                                               DegradeMessageConfig degradeMessageConfig) {
        return new AlertMachineCompensateJob(degradeMessageCompensateService, degradeMessageConfig);
    }


}
