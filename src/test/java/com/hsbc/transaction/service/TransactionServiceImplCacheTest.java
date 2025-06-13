package com.hsbc.transaction.service;

import com.hsbc.transaction.common.TransactionNotFoundException;
import com.hsbc.transaction.enums.TransactionType;
import com.hsbc.transaction.model.Transaction;
import com.hsbc.transaction.repository.TransactionRepository;
import com.hsbc.transaction.request.UpdateTransactionRequest;
import com.hsbc.transaction.response.TransactionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
@EnableCaching
@ActiveProfiles("test")
class TransactionServiceImplCacheTest {

    @MockitoBean
    private TransactionRepository transactionRepository;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private TransactionServiceImpl transactionService;

    private Transaction transaction;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        transaction = new Transaction("tx123", "acc1", new BigDecimal("100.00"), TransactionType.DEPOSIT, LocalDateTime.now(), "desc");
        Cache cache = cacheManager.getCache("transactions");
        if (cache != null) {
            cache.clear();
        }
    }

    @Test
    void getTransactionById_shouldCacheResult() {
        when(transactionRepository.findById("tx123")).thenReturn(transaction);
        TransactionResponse first = transactionService.getTransactionById("tx123");
        TransactionResponse second = transactionService.getTransactionById("tx123");
        assertThat(first).isEqualTo(second);
        // 因为走了缓存，所以只调用一次数据库
        verify(transactionRepository, times(1)).findById("tx123");
    }

    @Test
    void updateTransaction_shouldEvictCache() {
        when(transactionRepository.findById("tx123")).thenReturn(transaction);
        when(transactionRepository.update(any(Transaction.class))).thenReturn(transaction);
        transactionService.getTransactionById("tx123");
        UpdateTransactionRequest req = new UpdateTransactionRequest();
        req.setAmount(new BigDecimal("200.00"));
        req.setType(TransactionType.DEPOSIT);
        req.setDescription("updated");
        transactionService.updateTransaction("tx123", req);
        transactionService.getTransactionById("tx123");
        // 因为 updateTransaction 方法使缓存失效了，但里面调用了 findById 方法，所以这里应该会调用三次 findById 方法
        verify(transactionRepository, times(3)).findById("tx123");
    }

    @Test
    void deleteTransaction_shouldEvictCache() {
        when(transactionRepository.findById("tx123")).thenReturn(transaction);
        when(transactionRepository.existsById("tx123")).thenReturn(true);
        doReturn(true).when(transactionRepository).deleteById("tx123");
        transactionService.getTransactionById("tx123");
        transactionService.deleteTransaction("tx123");
        transactionService.getTransactionById("tx123");
        // 因为 deleteTransaction 方法使缓存失效了，所以这里应该会调用两次 findById 方法
        verify(transactionRepository, times(2)).findById("tx123");
    }

    @Test
    void getTransactionById_notFound_shouldThrow() {
        when(transactionRepository.findById("not_exist")).thenReturn(null);
        // 测试 getTransactionById 方法，传入不存在的ID，不走缓存，应该会抛出 TransactionNotFoundException
        assertThrows(TransactionNotFoundException.class, () -> transactionService.getTransactionById("not_exist"));
    }
}

