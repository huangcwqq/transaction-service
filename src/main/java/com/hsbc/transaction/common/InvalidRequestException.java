package com.hsbc.transaction.common;

/**
 * 当尝试创建具有重复ID的交易时抛出的异常。
 */
public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message) {
        super(message);
    }
}
