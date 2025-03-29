package com.openquartz.mqdegrade.sender.core;

import com.openquartz.mqdegrade.sender.common.Pair;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 发送函数工厂
 *
 * @author svnee
 */
public class DirectSendFunctionFactory {

    private DirectSendFunctionFactory() {
    }

    private static final Map<String, Pair<Class<?>, Function<?, Boolean>>> directSendFuncMap = new HashMap<>();

    public static synchronized <T> void register(String key, Class<T> clazz, Function<T, Boolean> directSendFunc) {
        directSendFuncMap.put(key, new Pair<>(clazz, directSendFunc));
    }

    public static Pair<Class<?>, Function<?, Boolean>> get(String key) {
        return directSendFuncMap.get(key);
    }

}
