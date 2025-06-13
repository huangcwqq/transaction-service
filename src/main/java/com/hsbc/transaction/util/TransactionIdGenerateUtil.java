package com.hsbc.transaction.util;

import java.util.UUID;

/**
 * 用于生成唯一ID，比如交易ID 和防重token
 */
public class TransactionIdGenerateUtil {

    /**
     * 生成交易ID
     * @return 交易ID
     */
    public static String generateTransactionId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
