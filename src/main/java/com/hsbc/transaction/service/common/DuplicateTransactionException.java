package com.hsbc.transaction.service.common;

/**
 * 当尝试创建具有重复ID的交易时抛出的异常。
 */
public class DuplicateTransactionException extends RuntimeException {
    public DuplicateTransactionException(String message) {
        super(message);
    }
}
