package com.openquartz.mqdegrade.sender.common.exception;

public class DegradeException extends RuntimeException {

    public DegradeException(String message) {
        super(message);
    }

    public DegradeException(String message, Throwable cause) {
        super(message, cause);
    }
}
