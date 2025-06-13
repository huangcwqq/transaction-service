package com.hsbc.transaction.common;

public interface TransactionErrors {
    // 重复的交易
    int REPEAT_TRANSACTION = 10000;
    // 交易信息不存在
    int TRANSACTION_NOT_FOUND = 10001;
    // 无效的token
    int INVALID_TOKEN = 10002;
    // 非法请求
    int ILLEGAL_REQUEST = 10003;
    // 重复请求
    int DUPLICATE_REQUEST = 10004;

    static BusinessException repeatTransaction(String errorMessage) {
        return new BusinessException(REPEAT_TRANSACTION,errorMessage);
    }

    static BusinessException transactionNotFound(String errorMessage) {
        return new BusinessException(TRANSACTION_NOT_FOUND, errorMessage);
    }

    static BusinessException invalidToken() {
        return new BusinessException(INVALID_TOKEN, "无效的token");
    }

    static BusinessException illegalRequest() {
        return new BusinessException(ILLEGAL_REQUEST, "非法请求");
    }

    static BusinessException duplicateRequest() {
        return new BusinessException(DUPLICATE_REQUEST, "重复请求");
    }
}
