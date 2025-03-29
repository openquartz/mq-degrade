package com.openquartz.mqdegrade.sender.starter.autoconfig.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import static com.openquartz.mqdegrade.sender.starter.autoconfig.property.DegradePropertyBeanNameConstants.DEGRADE_PARALLEL_TRANSFER_PROPERTY_BEAN_NAME;
import static com.openquartz.mqdegrade.sender.starter.autoconfig.property.DegradePropertyPrefixConstants.TRANSFER;

@Data
@RefreshScope
@Component(DEGRADE_PARALLEL_TRANSFER_PROPERTY_BEAN_NAME)
@ConfigurationProperties(prefix = TRANSFER)
public class DegradeParallelTransferProperty {

    /**
     * 是否开启并行降级传输
     */
    private boolean enable;

    /**
     * 并行降级线程池配置
     */
    private DegradeParallelTransferThreadPoolProperty threadPool = new DegradeParallelTransferThreadPoolProperty();

    @Data
    public static class DegradeParallelTransferThreadPoolProperty {

        /**
         * 线程池名称前缀
         */
        private String threadPrefix = "mq-parallel-transfer-thread";

        /**
         * 线程池核心现成数量
         */
        private int corePoolSize = Runtime.getRuntime().availableProcessors();

        /**
         * 线程池线程空闲时间,单位:秒
         */
        private long keepAliveTime = 30;

        /**
         * 线程中最大数量
         */
        private int maxPoolSize = Runtime.getRuntime().availableProcessors() * 2;

        /**
         * 线程池中线程等待队列容量
         */
        private int queueCapacity = Runtime.getRuntime().availableProcessors() * 2;
    }

}
