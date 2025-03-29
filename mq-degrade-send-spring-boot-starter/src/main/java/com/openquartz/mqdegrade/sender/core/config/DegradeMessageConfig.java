package com.openquartz.mqdegrade.sender.core.config;

import com.openquartz.mqdegrade.sender.common.concurrent.DirectExecutor;

import java.util.concurrent.Executor;

/**
 * 降级消息配置
 * @author svnee
 */
public interface DegradeMessageConfig {

    /**
     * 默认降级资源配置前缀
     */
    String DEFAULT_MQ_DEGRADE_SENTINEL_RESOURCE_PREFIX = "MQ-DEGRADE.";

    /**
     * 是否强制降级传输
     *
     * @param resource 资源
     * @return 是否开启强制降级传输
     */
    boolean isEnableForceDegrade(String resource);

    /**
     * 是否开启自动降级传输
     *
     * @param resource 资源
     * @return 降级传输
     */
    boolean isEnableAutoDegrade(String resource);

    /**
     * 针对自动降级，获取降级传输的资源。默认是针对资源降级
     * @param resource 资源
     * @return sentinel降级资源点
     */
    default String getAutoDegradeTransferSentinelResource(String resource) {
        return DEFAULT_MQ_DEGRADE_SENTINEL_RESOURCE_PREFIX + resource;
    }

    /**
     * 是否开启并行降级传输。默认串行
     * @param resource 资源
     * @return 是否开启并行降级传输
     */
    default boolean isEnableParallelDegradeTransfer(String resource) {
        return false;
    }

    /**
     * 获取并行传输线程池
     * @return 线程池
     */
    default Executor getParallelDegradeTransferExecutor() {
        return new DirectExecutor();
    }

    /**
     * 补偿策略的间隔时间，单位: s
     * @return 间隔时间
     */
    default Integer getCompensateGlobalBackOffIntervalTime() {
        return 3600;
    }

    /**
     * 获取当前机器执行补偿时间，单位：s
     * @return 间隔时间
     */
    default Integer getCompensateSelfBackOffIntervalTime() {
        return 600;
    }

    /**
     * 补偿策略最大重试次数
     * @return 最大重试次数
     */
    default Integer getCompensateMaxRetryCount(){
        return 15;
    }

    /**
     * 补偿策略一次限制条数
     * @return 补偿策略条数
     */
    default Integer getCompensateLimitCount(){
        return 10;
    }

    /**
     * 补偿策略延时时间
     * @return 补偿延时时间
     */
    default long getCompensateSelfJobDelayTime(){
        return 10;
    }

    /**
     * 补偿周期时间,单位：s
     * @return 周期时间
     */
    default long getCompensateSelfJobPeriodTime(){
        return 300;
    }

    /**
     * 补偿延时时间 单位：s
     * @return 补偿延时时间
     */
    default long getCompensateGlobalJobDelayTime(){
        return 20;
    }

    /**
     * 补偿周期 单位：s
     * @return 补偿周期
     */
    default long getCompensateGlobalJobPeriodTime(){
        return 900;
    }

    /**
     * 告警延时时间，单位：s
     * @return 延时时间
     */
    default long getCompensateAlertJobDelayTime(){
        return 60;
    }

    /**
     * 告警周期 单位：s
     * @return 周期时长
     */
    default long getCompensateAlertJobPeriodTime(){
        return 600;
    }
}
