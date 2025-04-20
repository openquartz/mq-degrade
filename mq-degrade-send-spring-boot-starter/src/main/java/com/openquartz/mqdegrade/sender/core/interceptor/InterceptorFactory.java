package com.openquartz.mqdegrade.sender.core.interceptor;

import java.util.*;

/**
 * InterceptorFactory
 *
 * @author svnee
 */
public class InterceptorFactory {

    private InterceptorFactory() {
    }

    /**
     * 发送拦截器
     */
    private static final PriorityQueue<SendInterceptor> SEND_INTERCEPTOR_SET = new PriorityQueue<>(Comparator.comparingInt(SendInterceptor::order));

    /**
     * 降级传输拦截器
     */
    private static final PriorityQueue<DegradeTransferInterceptor> DEGRADE_TRANSFER_INTERCEPTOR_SET = new PriorityQueue<>(Comparator.comparingInt(DegradeTransferInterceptor::order));

    /**
     * 注册拦截器
     *
     * @param sendInterceptor 发送拦截器
     */
    public synchronized static void register(SendInterceptor sendInterceptor) {
        if (SEND_INTERCEPTOR_SET.contains(sendInterceptor)) {
            return;
        }
        SEND_INTERCEPTOR_SET.add(sendInterceptor);
    }

    /**
     * 注册拦截器
     *
     * @param sendInterceptor 发送拦截器
     */
    public synchronized static void register(DegradeTransferInterceptor sendInterceptor) {
        if (DEGRADE_TRANSFER_INTERCEPTOR_SET.contains(sendInterceptor)) {
            return;
        }
        DEGRADE_TRANSFER_INTERCEPTOR_SET.add(sendInterceptor);
    }

    /**
     * 获取发送拦截器
     *
     * @return 拦截器
     */
    public static Collection<SendInterceptor> getSendInterceptor() {
        return SEND_INTERCEPTOR_SET;
    }

    /**
     * 获取发送拦截器
     *
     * @return 拦截器
     */
    public static Collection<DegradeTransferInterceptor> getDegradeTransferInterceptor() {
        return DEGRADE_TRANSFER_INTERCEPTOR_SET;
    }

    /**
     * 清除拦截器
     */
    public static void clear() {
        SEND_INTERCEPTOR_SET.clear();
        DEGRADE_TRANSFER_INTERCEPTOR_SET.clear();
    }

}
