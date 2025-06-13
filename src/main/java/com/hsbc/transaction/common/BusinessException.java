package com.hsbc.transaction.common;

public class BusinessException extends RuntimeException {

    private final int code;

    private Object[] args;

    public BusinessException(int code, String error, Object... args) {
        super(error);
        this.code = code;
        this.args = args;
    }
}
