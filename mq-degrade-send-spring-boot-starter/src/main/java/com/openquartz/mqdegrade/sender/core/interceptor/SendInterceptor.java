package com.openquartz.mqdegrade.sender.core.interceptor;

import org.springframework.lang.Nullable;

/**
 * 发送拦截器支持
 *
 * @author svnee
 */
public interface SendInterceptor {

    /**
     * 越小越先执行
     * @return 优先级
     */
    int order();

    /**
     * 发送前拦截
     *
     * @param message  消息
     * @param resource 资源
     */
    void beforeSend(Object message, String resource);

    /**
     * 发送完成后拦截
     *
     * @param message  消息
     * @param resource 资源
     * @param success  是否成功
     * @param ex       执行异常
     */
    void afterComplete(Object message, String resource, boolean success, @Nullable Throwable ex);
}
