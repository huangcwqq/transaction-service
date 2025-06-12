package com.hsbc.transaction.service.util;

import java.util.UUID;

public class IdGenerateUtil {
    public static String generateId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String generateTransactionId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
