package com.openquartz.mqdegrade.sender.core.factory;

import com.openquartz.mqdegrade.sender.common.Pair;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * 降级路由工厂
 *
 * @author svnee
 */
public class DegradeRouterFactory {

    private DegradeRouterFactory() {
    }

    /**
     * 注册降级传输列表
     */
    private static final Map<String, Map<String, Pair<Class<?>, Predicate<?>>>> RESOURCE_2_DEGRADE_LIST_MAP = new ConcurrentHashMap<>();

    /**
     * 注册降级传输
     *
     * @param resource     资源/topic
     * @param messageClazz 消息类型
     * @param predicate    是否发送成功方法
     * @param <T>          T
     */
    public static <T> void register(String resource, String degradeResource, Class<T> messageClazz, Predicate<T> predicate) {
        RESOURCE_2_DEGRADE_LIST_MAP
                .computeIfAbsent(resource, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(degradeResource, k -> new Pair<>(messageClazz, predicate));
    }

    /**
     * 获取降级传输列表
     *
     * @param resource 资源/topic
     * @return 降级传输列表
     */
    public static Map<String, Pair<Class<?>, Predicate<?>>> get(String resource) {
        return RESOURCE_2_DEGRADE_LIST_MAP.get(resource);
    }

    /**
     * 清除所有的注册列表
     */
    public static void clear() {
        RESOURCE_2_DEGRADE_LIST_MAP.clear();
    }

}
