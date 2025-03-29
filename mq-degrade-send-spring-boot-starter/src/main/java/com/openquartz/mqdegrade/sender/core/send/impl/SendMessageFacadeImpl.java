package com.openquartz.mqdegrade.sender.core.send.impl;

import com.openquartz.mqdegrade.sender.common.Pair;
import com.openquartz.mqdegrade.sender.common.utils.SerdeUtils;
import com.openquartz.mqdegrade.sender.core.config.DegradeMessageConfig;
import com.openquartz.mqdegrade.sender.core.factory.DegradeRouterFactory;
import com.openquartz.mqdegrade.sender.core.factory.SendRouterFactory;
import com.openquartz.mqdegrade.sender.core.send.DegradeMessageFilter;
import com.openquartz.mqdegrade.sender.core.send.IMessage;
import com.openquartz.mqdegrade.sender.core.send.SendMessageFacade;

import static com.openquartz.mqdegrade.sender.common.exception.ExceptionUtils.wrapAndThrow;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
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

    private final DegradeMessageConfig mqDegradeMessageConfig;
    private final DegradeMessageStorageService degradeMessageStorageService;
    private final DegradeMessageFilter degradeMessageFilter;

    public SendMessageFacadeImpl(DegradeMessageConfig mqDegradeMessageConfig,
                                 DegradeMessageStorageService degradeMessageStorageService,
                                 DegradeMessageFilter degradeMessageFilter) {
        this.mqDegradeMessageConfig = mqDegradeMessageConfig;
        this.degradeMessageStorageService = degradeMessageStorageService;
        this.degradeMessageFilter = degradeMessageFilter;
    }

    @Override
    public <T> boolean send(T message, String resource) {
        return doSend(message, resource);
    }

    private <T> boolean doSend(T message, String resource) {

        // 1、降级配置开关。是否开启强制降级传输。
        if (mqDegradeMessageConfig.isEnableForceDegrade(resource)) {
            return degradeTransfer(message, resource);
        }

        // 2、是否开启自动降级开关。是否满足自动降级条件。(配置sentinel配置降级条件).
        if (mqDegradeMessageConfig.isEnableAutoDegrade(resource)) {
            // 开启自动降级时,调用sentinel.决定是否自动降级传输。
            Entry entry = null;
            try {
                entry = SphU.entry(mqDegradeMessageConfig.getAutoDegradeTransferSentinelResource(resource));
                return directSend(message, resource);
            } catch (BlockException ex) {
                log.info("[SendMessageFacade#send] 触发自动降级！resource:{}", resource);
                return degradeTransfer(message, resource);
            } finally {
                if (entry != null) {
                    entry.exit();
                }
            }
        }

        return directSend(message, resource);
    }

    private <T> boolean degradeTransfer(T message, String resource) {

        Map<String, String> attributeMap = new HashMap<>();

        // 如果降级传输失败
        if (!doDegradeTransfer(message, resource)) {
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
        return Boolean.TRUE.equals(sendFunction.test(message));
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

        List<Pair<Class<?>, Predicate<?>>> pairList = DegradeRouterFactory.get(resource);
        if (CollectionUtils.isEmpty(pairList)) {
            return true;
        }

        Exception anyException = null;
        boolean degradeTransferResult = true;
        if (!mqDegradeMessageConfig.isEnableParallelDegradeTransfer(resource)) {

            // 循环降级执行
            for (Pair<Class<?>, Predicate<?>> degradeFuncPair : pairList) {
                Class<?> k = degradeFuncPair.getKey();
                Object object = SerdeUtils.serdeConvert(message, k);
                Predicate degradeFunction = degradeFuncPair.getValue();
                try {
                    // 降级结果
                    degradeTransferResult = degradeTransferResult && degradeFunction.test(object);
                } catch (Exception ex) {
                    anyException = ex;
                }
            }

            if (anyException != null) {
                return wrapAndThrow(anyException);
            }

            return degradeTransferResult;
        }

        // 开启了并行降级传输
        Executor parallelDegradeExecutor = mqDegradeMessageConfig.getParallelDegradeTransferExecutor();
        List<CompletableFuture<Boolean>> futureList = new ArrayList<>();
        for (Pair<Class<?>, Predicate<?>> degradeFuncPair : pairList) {

            Class<?> k = degradeFuncPair.getKey();
            Object object = SerdeUtils.serdeConvert(message, k);
            Predicate degradeFunction = degradeFuncPair.getValue();

            CompletableFuture<Boolean> future = CompletableFuture
                    .supplyAsync(() -> degradeFunction.test(object), parallelDegradeExecutor);
            futureList.add(future);
        }

        for (CompletableFuture<Boolean> future : futureList) {
            try {
                degradeTransferResult = degradeTransferResult && future.get();
            } catch (Exception ex) {
                anyException = ex;
            }
        }

        if (anyException != null) {
            return wrapAndThrow(anyException);
        }

        return degradeTransferResult;
    }
}