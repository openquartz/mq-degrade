package com.openquartz.mqdegrade.sender.common.utils;

/**
 * StringUtils
 *
 * @author svnee
 */
public class StringUtils {

    private StringUtils() {
    }

    public static final String EMPTY = "";

    public boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}
