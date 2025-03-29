package com.openquartz.mqdegrade.sender.common.utils;

/**
 * JsonConvertor
 * @author svnee
 */
public interface JsonConvertor {

    /**
     * writeAsJson
     * @param object obj
     * @return convertor
     */
    String writeAsJson(Object object);

    /**
     * readAsJavaType
     * @param json json
     * @param classOfT ClassT
     * @return java obj
     * @param <T> T
     */
    <T> T readAsJavaType(String json, Class<T> classOfT);
}
