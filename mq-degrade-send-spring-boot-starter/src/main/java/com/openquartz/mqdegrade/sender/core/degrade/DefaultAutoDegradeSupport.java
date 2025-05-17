package com.openquartz.mqdegrade.sender.core.degrade;

import com.openquartz.mqdegrade.sender.common.exception.DegradeException;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

/***
 * Default Auto Degrade Support
 * @author svnee
 */
@Slf4j
public class DefaultAutoDegradeSupport implements AutoDegradeSupport {

    @Override
    public boolean autoDegrade(String resource, Function<String, Boolean> degradeTransferFunc, Function<String, Boolean> directSendFunc) {
        log.error("[DefaultAutoDegradeSupport] resource:{} not support auto-degrade!", resource);
        throw new DegradeException(String.format("Resource %s not support auto degrade! you need provide support this function!", resource));
    }
}
