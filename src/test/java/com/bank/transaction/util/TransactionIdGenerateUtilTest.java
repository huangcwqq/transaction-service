package com.bank.transaction.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

class TransactionIdGenerateUtilTest {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    // 用于重置计数器
    @BeforeEach
    void setUp() throws Exception {
        // 使用反射重置计数器
        Field field = TransactionIdGenerateUtil.class.getDeclaredField("counter");
        field.setAccessible(true);
        AtomicInteger counter = (AtomicInteger) field.get(null);
        counter.set(0); // 重置计数器
    }

    /**
     * 测试正常生成交易ID的格式是否正确
     */
    @Test
    void testGenerateTransactionId_FormatCorrect() {
        String id = TransactionIdGenerateUtil.generateTransactionId();

        // 验证总长度
        assertEquals(14 + 3 + 3, id.length());

        // 验证前14位是合法的时间格式
        String timestampPart = id.substring(0, 14);
        assertDoesNotThrow(() -> LocalDateTime.parse(timestampPart, formatter));

        // 验证中间三位是数字
        String randomPart = id.substring(14, 17);
        assertTrue(randomPart.matches("\\d{3}"));

        // 验证最后三位是数字
        String sequencePart = id.substring(17);
        assertTrue(sequencePart.matches("\\d{3}"));
    }

    /**
     * 测试计数器在超过999时是否会循环
     */
    @Test
    void testCounter_OverflowsAndResets() throws Exception {
        Field field = TransactionIdGenerateUtil.class.getDeclaredField("counter");
        field.setAccessible(true);
        AtomicInteger counter = (AtomicInteger) field.get(null);

        // 设置计数器为 999
        counter.set(999);
        String id1 = TransactionIdGenerateUtil.generateTransactionId();
        assertEquals("999", id1.substring(17)); // 第1000个应为 999

        String id2 = TransactionIdGenerateUtil.generateTransactionId();
        assertEquals("000", id2.substring(17)); // 第1001个应为 000
    }

    /**
     * 测试随机数范围是否为三位数
     */
    @Test
    void testRandomPart_IsThreeDigits() {
        for (int i = 0; i < 100; i++) {
            String id = TransactionIdGenerateUtil.generateTransactionId();
            String randomPart = id.substring(14, 17);
            int value = Integer.parseInt(randomPart);
            assertTrue(value >= 100 && value <= 999);
        }
    }

    /**
     * 测试连续调用时序号递增
     */
    @Test
    void testSequenceNumber_IncreasesWithEachCall() {
        String id1 = TransactionIdGenerateUtil.generateTransactionId();
        String id2 = TransactionIdGenerateUtil.generateTransactionId();
        String id3 = TransactionIdGenerateUtil.generateTransactionId();

        assertEquals("000", id1.substring(17));
        assertEquals("001", id2.substring(17));
        assertEquals("002", id3.substring(17));
    }
}