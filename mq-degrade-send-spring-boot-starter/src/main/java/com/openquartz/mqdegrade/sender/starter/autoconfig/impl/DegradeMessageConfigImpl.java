package com.openquartz.mqdegrade.sender.starter.autoconfig.impl;

import com.openquartz.mqdegrade.sender.core.config.DegradeMessageConfig;
import com.openquartz.mqdegrade.sender.starter.autoconfig.property.DegradeCommonProperty;
import com.openquartz.mqdegrade.sender.starter.autoconfig.property.DegradeCompensateProperty;
import com.openquartz.mqdegrade.sender.starter.autoconfig.property.DegradeParallelTransferProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;

@Slf4j
public class DegradeMessageConfigImpl implements DegradeMessageConfig {

    private final DegradeCommonProperty degradeCommonProperty;
    private final DegradeCompensateProperty degradeCompensateProperty;
    private final DegradeParallelTransferProperty degradeParallelTransferProperty;
    private final Executor parallelDegradeThreadPoolExecutor;

    public DegradeMessageConfigImpl(DegradeCommonProperty degradeCommonProperty,
                                    DegradeCompensateProperty degradeCompensateProperty,
                                    DegradeParallelTransferProperty degradeParallelTransferProperty,
                                    Executor parallelDegradeThreadPoolExecutor) {
        this.degradeCommonProperty = degradeCommonProperty;
        this.degradeCompensateProperty = degradeCompensateProperty;
        this.degradeParallelTransferProperty = degradeParallelTransferProperty;
        this.parallelDegradeThreadPoolExecutor = parallelDegradeThreadPoolExecutor;
    }

    @Override
    public boolean isEnableForceDegrade(String resource) {

        if (!degradeCommonProperty.isEnable()) {
            log.info("[DegradeMessageConfig#isEnableForceDegrade] mq-degrade is disabled");
            return false;
        }

        if (degradeCommonProperty.isEnableForceDegrade()) {
            return true;
        }

        DegradeCommonProperty.ResourceDegradeProperty degradeProperty = degradeCommonProperty.getResourceDegrade().get(resource);
        if (degradeProperty != null) {
            return degradeProperty.isEnableForceDegrade();
        }

        return false;
    }

    @Override
    public boolean isEnableAutoDegrade(String resource) {

        if (!degradeCommonProperty.isEnable()) {
            log.info("[DegradeMessageConfig#isEnableAutoDegrade] mq-degrade is disabled!");
            return false;
        }

        if (!degradeCommonProperty.isEnableAutoDegrade()) {
            log.info("[DegradeMessageConfig#isEnableAutoDegrade] mq-degrade disable auto-degrade!");
            return false;
        }

        DegradeCommonProperty.ResourceDegradeProperty degradeProperty = degradeCommonProperty.getResourceDegrade().get(resource);
        if (degradeProperty == null || !degradeProperty.isEnableAutoDegrade()) {
            log.info("[DegradeMessageConfig#isEnableAutoDegrade] this resource:{} degrade is not enabled auto-degrade!", resource);
            return false;
        }

        return true;
    }

    @Override
    public String getAutoDegradeTransferResource(String resource) {
        DegradeCommonProperty.ResourceDegradeProperty degradeProperty = degradeCommonProperty.getResourceDegrade().get(resource);
        if (Objects.isNull(degradeProperty)
                || StringUtils.isEmpty(degradeProperty.getAutoDegradeSentinelResource())
                ||StringUtils.isEmpty(degradeProperty.getAutoDegradeSentinelResource().trim())) {
            return DegradeMessageConfig.super.getAutoDegradeTransferResource(resource);
        }
        return degradeProperty.getAutoDegradeSentinelResource();
    }

    @Override
    public boolean isEnableParallelDegradeTransfer(String resource) {
        if (degradeParallelTransferProperty.isEnable()){
            return true;
        }
        DegradeCommonProperty.ResourceDegradeProperty degradeProperty = degradeCommonProperty.getResourceDegrade().get(resource);
        return Optional
                .ofNullable(degradeProperty)
                .map(DegradeCommonProperty.ResourceDegradeProperty::isEnableParallelDegradeTransfer)
                .orElse(false);
    }

    @Override
    public Executor getParallelDegradeTransferExecutor() {

        return Optional
                .ofNullable(parallelDegradeThreadPoolExecutor)
                .orElse( DegradeMessageConfig.super.getParallelDegradeTransferExecutor());
    }

    @Override
    public Integer getCompensateGlobalBackOffIntervalTime() {
        return degradeCompensateProperty.getGlobal().getBackoffIntervalTime();
    }

    @Override
    public Integer getCompensateSelfBackOffIntervalTime() {
        return degradeCompensateProperty.getSelf().getBackoffIntervalTime();
    }

    @Override
    public Integer getCompensateMaxRetryCount() {
        return degradeCompensateProperty.getMaxRetryCount();
    }

    @Override
    public Integer getCompensateLimitCount() {
        return degradeCompensateProperty.getLimitCount();
    }

    @Override
    public long getCompensateSelfJobDelayTime() {
        return degradeCompensateProperty.getSelf().getDelayTime();
    }

    @Override
    public long getCompensateSelfJobPeriodTime() {
        return degradeCompensateProperty.getSelf().getPeriodTime();
    }

    @Override
    public long getCompensateGlobalJobDelayTime() {
        return degradeCompensateProperty.getGlobal().getDelayTime();
    }

    @Override
    public long getCompensateGlobalJobPeriodTime() {
        return degradeCompensateProperty.getGlobal().getPeriodTime();
    }

    @Override
    public long getCompensateAlertJobDelayTime() {
        return degradeCompensateProperty.getAlert().getDelayTime();
    }

    @Override
    public long getCompensateAlertJobPeriodTime() {
        return degradeCompensateProperty.getAlert().getPeriodTime();
    }
}
