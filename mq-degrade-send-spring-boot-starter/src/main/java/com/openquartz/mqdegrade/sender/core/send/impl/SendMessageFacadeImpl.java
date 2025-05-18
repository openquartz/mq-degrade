package com.openquartz.mqdegrade.sender.core.send.impl;

import com.openquartz.mqdegrade.sender.common.Pair;
import com.openquartz.mqdegrade.sender.common.exception.DegradeException;
import com.openquartz.mqdegrade.sender.common.exception.ExceptionUtils;
import com.openquartz.mqdegrade.sender.common.utils.SerdeUtils;
import com.openquartz.mqdegrade.sender.core.config.DegradeMessageConfig;
import com.openquartz.mqdegrade.sender.core.degrade.AutoDegradeSupport;
import com.openquartz.mqdegrade.sender.core.factory.DegradeRouterFactory;
import com.openquartz.mqdegrade.sender.core.factory.SendRouterFactory;
import com.openquartz.mqdegrade.sender.core.interceptor.DegradeTransferInterceptor;
import com.openquartz.mqdegrade.sender.core.interceptor.SendInterceptor;
import com.openquartz.mqdegrade.sender.core.interceptor.InterceptorFactory;
import com.openquartz.mqdegrade.sender.core.send.DegradeMessageFilter;
import com.openquartz.mqdegrade.sender.core.send.IMessage;
import com.openquartz.mqdegrade.sender.core.send.SendMessageFacade;

import static com.openquartz.mqdegrade.sender.common.exception.ExceptionUtils.wrapAndThrow;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.Predicate;

import com.openquartz.mqdegrade.sender.persist.service.DegradeMessageStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

/**
 * @author svnee
 */
@Slf4j
public class SendMessageFacadeImpl implements SendMessageFacade {

    private final DegradeMessageConfig degradeMessageConfig;
    private final DegradeMessageStorageService degradeMessageStorageService;
    private final DegradeMessageFilter degradeMessageFilter;
    private final AutoDegradeSupport autoDegradeSupport;

    public SendMessageFacadeImpl(DegradeMessageConfig degradeMessageConfig,
                                 DegradeMessageStorageService degradeMessageStorageService,
                                 DegradeMessageFilter degradeMessageFilter, AutoDegradeSupport autoDegradeSupport) {
        this.degradeMessageConfig = degradeMessageConfig;
        this.degradeMessageStorageService = degradeMessageStorageService;
        this.degradeMessageFilter = degradeMessageFilter;
        this.autoDegradeSupport = autoDegradeSupport;
    }

    @Override
    public <T> boolean send(T message, String resource) {

        Collection<SendInterceptor> interceptorList = InterceptorFactory.getSendInterceptor();
        PriorityQueue<SendInterceptor> sendInterceptorQueue = new PriorityQueue<>(((o1, o2) -> o2.order() - o1.order()));
        Throwable executeEx = doBeforeIntercept(message, resource, interceptorList, sendInterceptorQueue);

        boolean executeResult = false;
        if (executeEx == null) {
            try {
                executeResult = doSend(message, resource);
            } catch (Throwable ex) {
                executeEx = ex;
            }
        }

        // 执行完成后拦截
        doCompleteIntercept(sendInterceptorQueue, message, resource, executeResult, executeEx);
        if (executeEx != null) {
            return ExceptionUtils.wrapAndThrow(executeEx);
        }
        return executeResult;
    }

    private static <T> Throwable doBeforeIntercept(T message, String resource, Collection<SendInterceptor> interceptorList, PriorityQueue<SendInterceptor> exeSuccessfullyInterceptorQueue) {
        try {
            for (SendInterceptor interceptor : interceptorList) {
                exeSuccessfullyInterceptorQueue.add(interceptor);
                interceptor.beforeSend(message, resource);
            }
        } catch (Throwable ex) {
            return ex;
        }
        return null;
    }

    private static <T> void doCompleteIntercept(PriorityQueue<SendInterceptor> sendInterceptorQueue, T message, String resource, boolean executeResult, Throwable executeEx) {

        if (sendInterceptorQueue.isEmpty()) {
            return;
        }

        while (!sendInterceptorQueue.isEmpty()) {
            SendInterceptor sendInterceptor = sendInterceptorQueue.poll();
            sendInterceptor.afterComplete(message, resource, executeResult, executeEx);
        }
    }

    private <T> boolean doSend(T message, String resource) {

        // 1、降级配置开关。是否开启强制降级传输。
        if (degradeMessageConfig.isEnableForceDegrade(resource)) {
            return degradeTransfer(message, resource);
        }

        // 2、是否开启自动降级开关。是否满足自动降级条件.
        if (degradeMessageConfig.isEnableAutoDegrade(resource)) {
            if (autoDegradeSupport == null) {
                throw new DegradeException(String.format("resource:%s auto-degrade not support! please config!", resource));
            }
            return autoDegradeSupport.autoDegrade(resource, res -> degradeTransfer(message, res), res -> directSend(message, res));
        }

        return directSend(message, resource);
    }

    private <T> boolean degradeTransfer(T message, String resource) {

        // 如果降级传输失败
        // 降级传输前执行
        Collection<DegradeTransferInterceptor> transferInterceptorList = InterceptorFactory.getDegradeTransferInterceptor();
        PriorityQueue<DegradeTransferInterceptor> exeSuccessfulInterceptorList = new PriorityQueue<>(((o1, o2) -> o2.order() - o1.order()));
        Throwable exeEx = doBeforeTransferIntercept(message, resource, transferInterceptorList, exeSuccessfulInterceptorList);

        // 执行降级传输
        boolean degradeTransferResult = false;
        try {
            degradeTransferResult = doDegradeTransfer(message, resource);
        } catch (Throwable ex) {
            exeEx = ex;
        }

        // 降级传输后拦截
        doAfterCompleteTransferIntercept(exeSuccessfulInterceptorList, message, resource, degradeTransferResult, exeEx);

        // 降级传输失败直接返回false
        if (!degradeTransferResult) {
            return false;
        }

        // 如果降级成功时保存降级消息
        if (degradeMessageFilter.filter(message, resource)) {
            return true;
        }

        degradeMessageStorageService.save(resource, SerdeUtils.toJson(message),
                message instanceof IMessage ? ((IMessage) message).key() : null);
        return true;
    }

    private <T> void doAfterCompleteTransferIntercept(PriorityQueue<DegradeTransferInterceptor> exeSuccessfulInterceptorList, T message, String resource, boolean degradeTransferResult, Throwable exeEx) {
        if (exeSuccessfulInterceptorList.isEmpty()) {
            return;
        }

        while (!exeSuccessfulInterceptorList.isEmpty()) {
            DegradeTransferInterceptor degradeTransferInterceptor = exeSuccessfulInterceptorList.poll();
            degradeTransferInterceptor.afterTransferComplete(message, resource, degradeTransferResult, exeEx);
        }
    }

    private <T> Throwable doBeforeTransferIntercept(T message, String resource, Collection<DegradeTransferInterceptor> transferInterceptorList, PriorityQueue<DegradeTransferInterceptor> exeSuccessfulInterceptorList) {
        try {
            for (DegradeTransferInterceptor interceptor : transferInterceptorList) {
                exeSuccessfulInterceptorList.add(interceptor);
                interceptor.beforeTransfer(message, resource);
            }
        } catch (Throwable ex) {
            return ex;
        }
        return null;
    }

    /**
     * 直接发送消息到对应的消息队列
     *
     * @param message  消息
     * @param resource 绑定资源
     * @param <T>      T
     * @return 发送结果
     */
    private <T> boolean directSend(T message, String resource) {

        Pair<Class<T>, Predicate<T>> classFunctionPair = SendRouterFactory.get(resource);

        // 直接发送
        Predicate<T> sendFunction = classFunctionPair.getValue();
        return sendFunction.test(message);
    }

    /**
     * 降级传输
     *
     * @param message  message
     * @param resource resource
     * @param <T>      T
     * @return 降级传输结果
     */
    @SuppressWarnings("all")
    private <T> boolean doDegradeTransfer(T message, String resource) {

        Map<String, Pair<Class<?>, Predicate<?>>> degradeTransferMap = DegradeRouterFactory.get(resource);
        if (CollectionUtils.isEmpty(degradeTransferMap)) {
            return true;
        }

        Exception anyException = null;
        boolean degradeTransferResult = true;
        // 降级传输失败的资源
        Set<String> failedDegreadeResourceList = ConcurrentHashMap.newKeySet();
        if (!degradeMessageConfig.isEnableParallelDegradeTransfer(resource)) {

            // 循环降级执行
            for (Map.Entry<String, Pair<Class<?>, Predicate<?>>> degradeFuncEntry : degradeTransferMap.entrySet()) {
                Class<?> degradeTransferMsgType = degradeFuncEntry.getValue().getKey();
                Object object = SerdeUtils.serdeConvert(message, degradeTransferMsgType);
                Predicate degradeFunction = degradeFuncEntry.getValue().getValue();
                try {
                    // 降级结果
                    degradeTransferResult = degradeTransferResult && degradeFunction.test(object);
                } catch (Exception ex) {
                    anyException = ex;
                    failedDegreadeResourceList.add(degradeFuncEntry.getKey());
                }
            }

            if (!failedDegreadeResourceList.isEmpty()) {
                log.error("[SendMessageFacade#doDegradeTransfer] resource:{},msg:{} degrade-send failed resource:{}",
                        resource, message, failedDegreadeResourceList);
            }

            if (anyException != null) {
                return wrapAndThrow(anyException);
            }

            return degradeTransferResult;
        }

        // 开启了并行降级传输
        Executor parallelDegradeExecutor = degradeMessageConfig.getParallelDegradeTransferExecutor();
        List<Pair<String,CompletableFuture<Boolean>>> futureList = new ArrayList<>();
        for (Map.Entry<String, Pair<Class<?>, Predicate<?>>> degradeFuncEntry : degradeTransferMap.entrySet()) {

            Class<?> degradeTransferMsgType = degradeFuncEntry.getValue().getKey();
            Object object = SerdeUtils.serdeConvert(message, degradeTransferMsgType);
            Predicate degradeFunction = degradeFuncEntry.getValue().getValue();

            CompletableFuture<Boolean> future = CompletableFuture
                    .supplyAsync(() -> degradeFunction.test(object), parallelDegradeExecutor);
            futureList.add(Pair.of(degradeFuncEntry.getKey(),future));
        }

        for (Pair<String, CompletableFuture<Boolean>> futurePair : futureList) {
            try {
                degradeTransferResult = degradeTransferResult && futurePair.getValue().get();
            } catch (Exception ex) {
                anyException = ex;
                failedDegreadeResourceList.add(futurePair.getKey());
            }
        }

        if (!failedDegreadeResourceList.isEmpty()) {
            log.error("[SendMessageFacade#doDegradeTransfer] resource:{},msg:{} degrade-send failed resource:{}",
                    resource, message, failedDegreadeResourceList);
        }

        if (anyException != null) {
            return wrapAndThrow(anyException);
        }

        return degradeTransferResult;
    }
}