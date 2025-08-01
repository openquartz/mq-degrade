package com.openquartz.mqdegrade.sender.core.degrade;

import java.util.function.Function;

/**
 * 自动降级支持
 * @author svnee
 */
public interface AutoDegradeSupport {

    /**
     * 是否自动降级
     *
     * @param resource 资源
     * @param degradeTransferFunc 降级转移
     * @param directSendFunc 直接发送
     * @return 传输结果
     */
    boolean autoDegrade(String resource, Function<String, Boolean> degradeTransferFunc, Function<String, Boolean> directSendFunc);
}
