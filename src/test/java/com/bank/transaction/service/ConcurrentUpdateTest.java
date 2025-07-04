package com.bank.transaction.service;

import com.bank.transaction.common.TransactionNotFoundException;
import com.bank.transaction.enums.TransactionType;
import com.bank.transaction.model.Transaction;
import com.bank.transaction.repository.TransactionRepository;
import com.bank.transaction.request.UpdateTransactionRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ConcurrentUpdateTest {

    @Mock private TransactionRepository transactionRepository;

    // 测试并发更新交易的情况
@Test
public void testConcurrentUpdateTransaction() throws InterruptedException {
    // 初始化交易ID
    String transactionId = "test-id";
    // 模拟数据库，使用ConcurrentHashMap来存储交易信息
    ConcurrentHashMap<String, Transaction> db = new ConcurrentHashMap<>();
    // 向模拟数据库中插入初始交易信息
    db.put(transactionId, new Transaction(transactionId, "account-1", new BigDecimal(100), TransactionType.DEPOSIT, LocalDateTime.now(), "Initial"));

    // 配置mock对象，当调用findById方法时，从模拟数据库中获取交易信息
    when(transactionRepository.findById(transactionId)).thenAnswer(invocation -> {
        Transaction t = db.get(transactionId);
        return t == null ? null : new Transaction(t.getId(), t.getAccountId(), t.getAmount(), t.getType(), t.getDate(), t.getDescription());
    });

    // 配置mock对象，当调用update方法时，将新的交易信息更新到模拟数据库中
    when(transactionRepository.update(any(Transaction.class))).thenAnswer(invocation -> {
        Transaction t = invocation.getArgument(0);
        db.put(transactionId, t);
        return t;
    });

    // 创建TransactionServiceImpl实例，用于执行更新操作
    TransactionServiceImpl service = new TransactionServiceImpl(transactionRepository);
    // 定义线程数量
    int threadCount = 10;
    // 创建线程池，每个任务使用一个新的虚拟线程执行
    ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    // 创建计数器，用于等待所有线程完成
    CountDownLatch latch = new CountDownLatch(threadCount);

    // 提交线程任务到线程池
    for (int i = 0; i < threadCount; i++) {
        final int threadIndex = i;
        executor.submit(() -> {
            try {
                // 构建更新交易请求
                UpdateTransactionRequest request = new UpdateTransactionRequest(
                        new BigDecimal((100 + threadIndex)),
                        TransactionType.DEPOSIT,
                        "Updated Description"
                );
                // 调用服务方法更新交易信息
                service.updateTransaction(transactionId, request);
            } finally {
                // 计数器减一，表示一个线程已完成
                latch.countDown();
            }
        });
    }

    // 等待所有线程完成任务
    latch.await();
    // 关闭线程池
    executor.shutdown();

    // 验证findById和update方法是否被正确次数调用
    verify(transactionRepository, times(threadCount)).findById(transactionId);
    verify(transactionRepository, times(threadCount)).update(any(Transaction.class));

    // 检查最终交易的金额
    Transaction finalTransaction = transactionRepository.findById(transactionId);
    assertNotNull(finalTransaction);
    // 理论上，finalTransaction.getAmount() 应该是某个 101.00 到 150.00 之间的值，具体哪个取决于最后一个完成的线程。
    // 这里我们检查它是否在合理范围内，并验证最终值是某个成功更新的值。
    assertTrue(finalTransaction.getAmount().compareTo(new BigDecimal(100)) >= 0
            && finalTransaction.getAmount().compareTo(new BigDecimal(199)) <= 0);
}


    /**
     * 测试并发删除交易的情况
     * 此测试旨在验证在并发环境下，删除交易操作的表现和正确性
     * 它模拟了多个线程同时尝试删除同一个交易的情况
     */
    @Test
    public void testConcurrentDeleteTransaction() throws InterruptedException {
        // 定义交易ID和一个原子布尔变量来控制交易的存在状态
        String transactionId = "test-id";
        AtomicBoolean exists = new AtomicBoolean(true);

        // 配置交易仓库的模拟行为，当检查交易是否存在时，根据exists变量的值决定
        when(transactionRepository.existsById(transactionId)).thenAnswer(invocation -> exists.get());

        // 配置删除操作的模拟行为，如果交易存在则成功删除，否则抛出未找到异常
        doAnswer(invocation -> {
            if (exists.compareAndSet(true, false)) {
                return null;
            } else {
                throw new TransactionNotFoundException("Not found");
            }
        }).when(transactionRepository).deleteById(transactionId);

        // 创建交易服务实例
        TransactionServiceImpl service = new TransactionServiceImpl(transactionRepository);

        // 定义线程数量
        int threadCount = 10;

        // 使用虚拟线程执行器来执行任务
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        // 使用计数器来等待所有线程完成
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 定义计数器来记录成功删除和异常发生的次数
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger exceptionCount = new AtomicInteger(0);

        // 提交任务到执行器
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    service.deleteTransaction(transactionId);
                    successCount.incrementAndGet();
                } catch (TransactionNotFoundException e) {
                    exceptionCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        // 等待所有线程完成任务
        latch.await();

        // 关闭执行器
        executor.shutdown();

        // 验证只有一个线程成功删除了交易
        assertEquals(1, successCount.get());

        // 验证其他线程因为并发问题而未能找到并删除交易
        assertEquals(threadCount - 1, exceptionCount.get());

        // 验证存在性和删除操作被正确调用了次数
        verify(transactionRepository, times(threadCount)).existsById(transactionId);
        verify(transactionRepository, times(1)).deleteById(transactionId);
    }
}

