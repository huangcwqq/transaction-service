package com.bank.transaction.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

public class TransactionIdGenerateUtil {

    // 格式化日期时间作为前缀，例如 "20250405133045"
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    // 原子计数器用于避免同一毫秒内重复
    private static final AtomicInteger counter = new AtomicInteger(0);

    /**
     * 生成带日期的交易流水号
     * 格式：{日期时间}{6位随机数}{3位递增序号}
     */
    public static String generateTransactionId() {
        String timestamp = LocalDateTime.now().format(formatter);
        int randomPart = (int) (Math.random() * 900) + 100; // 生成三位随机整数
        int sequence = counter.getAndIncrement() % 1000; // 循环使用三位序号

        return timestamp + randomPart + String.format("%03d", sequence);
    }
}
