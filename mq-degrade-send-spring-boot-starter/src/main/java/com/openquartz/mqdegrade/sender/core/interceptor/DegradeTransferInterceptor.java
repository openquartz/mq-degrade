package com.openquartz.mqdegrade.sender.core.interceptor;

import javax.annotation.Nullable;

/**
 * 降级传输拦截
 *
 * @author svnee
 */
public interface DegradeTransferInterceptor {

    /**
     * 拦截顺序
     *
     * @return order
     */
    int order();

    /**
     * 降级传输之前
     *
     * @param message  消息
     * @param resource 资源
     */
    void beforeTransfer(Object message, String resource);

    /**
     * 降级传输完成之后
     *
     * @param message  消息
     * @param resource 资源
     * @param success  是否降级传输成功
     * @param ex       执行异常
     */
    void afterTransferComplete(Object message, String resource, boolean success, @Nullable Throwable ex);
}
