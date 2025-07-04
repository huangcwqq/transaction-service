package com.bank.transaction.common;

/**
 * 当尝试重复创建交易请求时抛出的异常。
 */
public class DuplicateRequestException extends RuntimeException {
    public DuplicateRequestException(String message) {
        super(message);
    }
}
