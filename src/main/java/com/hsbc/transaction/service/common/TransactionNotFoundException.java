package com.hsbc.transaction.service.common;

/**
 * 当请求的交易不存在时抛出的异常。
 */
public class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException(String message) {
        super(message);
    }
}
