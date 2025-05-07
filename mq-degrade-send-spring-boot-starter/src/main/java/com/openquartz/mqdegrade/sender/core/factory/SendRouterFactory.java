package com.openquartz.mqdegrade.sender.core.factory;

import com.openquartz.mqdegrade.sender.common.Pair;
import com.openquartz.mqdegrade.sender.common.exception.DegradeException;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * 发送路由工厂
 * @author svnee
 */
public class SendRouterFactory {

    private SendRouterFactory() {
    }

    private static final Map<String, Pair<Class<?>,Predicate<?>>> RESOURCE_2_SEND_ROUTER_MAP = new ConcurrentHashMap<>();

    /**
     * 注册
     * @param resource 资源
     * @param messageClazz 消息类型
     * @param sendRouter 发送路由
     * @param <T> T
     */
    public static <T>void register(String resource, Class<T> messageClazz, Predicate<T> sendRouter) {
        RESOURCE_2_SEND_ROUTER_MAP.putIfAbsent(resource, new Pair<>(messageClazz, sendRouter));
    }

    /**
     * 获取资源发送路由
     * @param resource 资源
     * @return 路由
     * @param <T> 消息类型 T
     */
    @SuppressWarnings("unchecked")
    public static <T>Pair<Class<T>,Predicate<T>> get(String resource) {
        Pair<Class<?>, Predicate<?>> classPredicatePair = RESOURCE_2_SEND_ROUTER_MAP.get(resource);
        if (Objects.isNull(classPredicatePair)) {
            throw new DegradeException(String.format("resource:%s not config send function!",resource));
        }
        return Pair.of((Class<T>) classPredicatePair.getKey(), (Predicate<T>)classPredicatePair.getValue());
    }

    /**
     *
     */
    public static void clear() {
        RESOURCE_2_SEND_ROUTER_MAP.clear();
    }
}
