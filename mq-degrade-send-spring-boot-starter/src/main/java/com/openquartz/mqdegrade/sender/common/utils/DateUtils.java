package com.openquartz.mqdegrade.sender.common.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    private DateUtils() {
    }

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static Date addSeconds(Date date, int seconds){
        return new Date(date.getTime() + seconds * 1000L);
    }


    public static Date addMinutes(Date date, Integer stepMinutes) {
        return new Date(date.getTime() + stepMinutes * 60 * 1000L);
    }

    public static String format(Date date, String pattern){
        return new SimpleDateFormat(pattern).format(date);
    }
}
