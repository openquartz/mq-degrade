package com.openquartz.mqdegrade.sender.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openquartz.mqdegrade.sender.common.exception.DegradeException;
import com.openquartz.mqdegrade.sender.common.exception.ExceptionUtils;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * JacksonConvertor
 *
 * @author svnee
 */
public class JacksonConvertor implements JsonConvertor {

    private final ObjectMapper mapper = newMapper();

    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    /**
     * 基于默认配置, 创建一个新{@link ObjectMapper},
     * 随后可以定制化这个新{@link ObjectMapper}.
     */
    public ObjectMapper newMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setDateFormat(dateFormat);
        objectMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        objectMapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
        objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
        return objectMapper;
    }

    /**
     * 获取默认{@link ObjectMapper}.
     * 直接使用默认{@link ObjectMapper}时需要小心,
     * 因为{@link ObjectMapper}类是可变的,
     * 对默认 ObjectMapper 的改动会影响所有默认ObjectMapper的依赖方.
     * 如果需要在当前上下文定制化{@link ObjectMapper},
     * 建议使用{@link #newMapper()}方法创建一个新的{@link ObjectMapper}.
     *
     * @see #newMapper()
     */
    public ObjectMapper mapper() {
        return mapper;
    }

    @Override
    public String writeAsJson(Object object) {
        if (object == null) {
            return StringUtils.EMPTY;
        }
        try {
            return mapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new DegradeException(String.format("object %s write as json error!", object));
        }
    }

    @Override
    public <T> T readAsJavaType(String json, Class<T> classOfT) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        try {
            return mapper.readValue(json, classOfT);
        } catch (IOException e) {
            return ExceptionUtils.wrapAndThrow(e);
        }
    }

}
