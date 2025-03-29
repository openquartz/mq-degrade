package com.openquartz.mqdegrade.sender.core;

import com.openquartz.mqdegrade.sender.common.Pair;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * @author svnee
 */
public class DegradeTransferFunctionFactory {

    private DegradeTransferFunctionFactory() {
    }

    private static final Map<String, Set<Pair<Class<?>, Function<?, Boolean>>>> degradeTransferFunctionMap = new ConcurrentHashMap<>();

    public static synchronized <T> void register(String key, Class<T> clazz, Function<T, Boolean> degradeTransferFunc) {
        Set<Pair<Class<?>, Function<?, Boolean>>> degradeTransferFuncSet =
            degradeTransferFunctionMap.getOrDefault(key, new LinkedHashSet<>());
        degradeTransferFuncSet.add(Pair.of(clazz, degradeTransferFunc));
        degradeTransferFunctionMap.put(key, degradeTransferFuncSet);
    }

    public static Set<Pair<Class<?>, Function<?, Boolean>>> get(String key) {
        return degradeTransferFunctionMap.get(key);
    }

}
