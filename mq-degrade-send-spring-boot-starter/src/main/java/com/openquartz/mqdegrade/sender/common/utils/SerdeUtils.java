package com.openquartz.mqdegrade.sender.common.utils;

import com.openquartz.mqdegrade.sender.common.exception.DegradeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * 序列号工具类
 *
 * @author svnee
 */
@Slf4j
public class SerdeUtils {

    private SerdeUtils() {
    }

    // 默认使用Jackson序列化
    private static JsonConvertor jsonConvertor = new JacksonConvertor();

    /**
     * register json convertor. 可自定义覆盖。
     *
     * @param convertor json convertor
     */
    public static void register(JsonConvertor convertor) {
        jsonConvertor = convertor;
    }

    public static String toJson(Object obj) {

        if (obj == null) {
            return null;
        }

        if (obj instanceof String) {
            return (String) obj;
        }

        if (obj instanceof byte[]) {
            return new String((byte[]) obj);
        }

        if (jsonConvertor == null) {
            throw new DegradeException("JsonConvertor not set!");
        }

        return jsonConvertor.writeAsJson(obj);
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        try {
            if (StringUtils.isEmpty(json) || StringUtils.isEmpty(json.trim())) {
                return null;
            }
            return jsonConvertor.readAsJavaType(json, classOfT);
        } catch (Exception e) {
            throw new DegradeException("Failed to convert json to " + classOfT, e);
        }
    }

    /**
     * 序列化反序列化转换
     *
     * @param obj   obj
     * @param clazz target class
     * @param <T>   source class
     * @param <R>   target class
     * @return target obj
     */
    @SuppressWarnings("unchecked")
    public static <T, R> R serdeConvert(T obj, Class<R> clazz) {

        if (obj == null) {
            return null;
        }

        if (clazz.isAssignableFrom(obj.getClass())) {
            return (R) obj;
        }

        if (clazz.isAssignableFrom(String.class)) {
            return (R) toJson(obj);
        }

        if (obj instanceof String) {
            return fromJson((String) obj, clazz);
        }

        if (obj instanceof byte[]) {
            return fromJson(new String((byte[]) obj), clazz);
        }

        try {
            return fromJson(toJson(obj), clazz);
        } catch (Exception e) {
            throw new DegradeException("Failed to convert json to " + clazz, e);
        }

    }
}
