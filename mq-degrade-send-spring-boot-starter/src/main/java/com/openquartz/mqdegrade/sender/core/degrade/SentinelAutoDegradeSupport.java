package com.openquartz.mqdegrade.sender.core.degrade;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.openquartz.mqdegrade.sender.core.config.DegradeMessageConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

/**
 * Sentinel 支持的自动降级配置
 *
 * @author svnee
 */
@Slf4j
public class SentinelAutoDegradeSupport implements AutoDegradeSupport {

    private final DegradeMessageConfig degradeMessageConfig;

    public SentinelAutoDegradeSupport(DegradeMessageConfig degradeMessageConfig) {
        this.degradeMessageConfig = degradeMessageConfig;
    }

    @Override
    public boolean autoDegrade(String resource, Function<String, Boolean> degradeTransferFunc, Function<String, Boolean> directSendFunc) {
        // 开启自动降级时,调用sentinel.决定是否自动降级传输。
        try (Entry ignored = SphU.entry(degradeMessageConfig.getAutoDegradeTransferResource(resource));) {
            return degradeTransferFunc.apply(resource);
        } catch (BlockException ex) {
            log.info("[SentinelAutoDegradeSupport#autoDegrade] 触发自动降级！resource:{}", resource);
            return directSendFunc.apply(resource);
        }
    }
}
