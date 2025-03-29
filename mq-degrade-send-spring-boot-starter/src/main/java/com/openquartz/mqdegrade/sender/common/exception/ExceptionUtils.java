package com.openquartz.mqdegrade.sender.common.exception;

import java.lang.reflect.UndeclaredThrowableException;

public class ExceptionUtils {

    private ExceptionUtils() {
    }

    public static <R> R wrapAndThrow(Throwable throwable) {

        if (throwable instanceof RuntimeException) {
            throw (RuntimeException) throwable;
        } else if (throwable instanceof Error) {
            throw (Error) throwable;
        } else {
            throw new UndeclaredThrowableException(throwable);
        }
    }


}
