package com.openquartz.mqdegrade.sender.common.utils;

/**
 * JacksonConvertor
 * @author svnee
 */
public class JacksonConvertor implements JsonConvertor {

    @Override
    public String writeAsJson(Object object) {
        return "";
    }

    @Override
    public <T> T readAsJavaType(String json, Class<T> classOfT) {
        return null;
    }

}
