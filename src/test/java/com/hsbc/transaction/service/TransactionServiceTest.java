package com.hsbc.transaction.service;

import static org.junit.jupiter.api.Assertions.*;

import com.hsbc.transaction.common.DuplicateTransactionException;
import com.hsbc.transaction.common.TransactionNotFoundException;
import com.hsbc.transaction.enums.TransactionType;
import com.hsbc.transaction.model.Transaction;
import com.hsbc.transaction.repository.TransactionRepository;
import com.hsbc.transaction.request.CreateTransactionRequest;
import com.hsbc.transaction.request.UpdateTransactionRequest;
import com.hsbc.transaction.response.TransactionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TransactionService的单元测试类。
 * 使用MockitoExtension启用Mockito注解，用于模拟依赖。
 */
@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock // 模拟TransactionRepository接口
    private TransactionRepository transactionRepository;

    @InjectMocks // 将模拟的依赖注入到TransactionServiceImpl实例中
    private TransactionServiceImpl transactionService;

    // 预设的交易ID，用于测试
    private String transactionId;
    // 预设的交易请求和实体
    private UpdateTransactionRequest UpdateTransactionRequest;
    private CreateTransactionRequest createTransactionRequest;
    private Transaction transaction;

    /**
     * 在每个测试方法执行前初始化数据。
     */
    @BeforeEach
    void setUp() {
        transactionId = "test-transaction-id";
        UpdateTransactionRequest = new UpdateTransactionRequest();
        UpdateTransactionRequest.setAmount(new BigDecimal("100.00"));
        UpdateTransactionRequest.setType(TransactionType.DEPOSIT);
        UpdateTransactionRequest.setDescription("Test Deposit");

        createTransactionRequest = new CreateTransactionRequest();
        createTransactionRequest.setId(transactionId);
        createTransactionRequest.setAccountId("account001");
        createTransactionRequest.setAmount(new BigDecimal("100.00"));
        createTransactionRequest.setType(TransactionType.DEPOSIT);
        createTransactionRequest.setDescription("Test Deposit");

        transaction = new Transaction(
                transactionId,
                "account001",
                new BigDecimal("100.00"),
                TransactionType.DEPOSIT,
                LocalDateTime.now(),
                "Test Deposit"
        );
    }

    /**
     * 测试成功创建交易的场景。
     */
    @Test
    void createTransaction_Success() {
        // 模拟行为：当调用 transactionRepository.existsById(anyString()) 时返回 false，
        // 表示ID不存在，可以创建。
        when(transactionRepository.existsById(anyString())).thenReturn(false);
        // 模拟行为：当调用 transactionRepository.save(any(Transaction.class)) 时，返回传入的交易对象。
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        // 执行待测试方法
        TransactionResponse response = transactionService.createTransaction(createTransactionRequest);

        // 验证结果：
        // 确保返回的响应不为空
        assertNotNull(response);
        // 确保金额匹配
        assertEquals(UpdateTransactionRequest.getAmount(), response.getAmount());
        // 确保类型匹配
        assertEquals(UpdateTransactionRequest.getType(), response.getType());
        // 确保描述匹配
        assertEquals(UpdateTransactionRequest.getDescription(), response.getDescription());

        // 验证Mock行为：
        // 验证 transactionRepository.existsById 方法被调用了1次，且参数是任意字符串。
        verify(transactionRepository, times(1)).existsById(anyString());
        // 验证 transactionRepository.save 方法被调用了1次，且参数是任意Transaction对象。
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    /**
     * 测试创建交易时ID重复的场景（尽管UUID冲突概率极低，但仍需测试）。
     */
    @Test
    void createTransaction_DuplicateId_ThrowsException() {
        // 模拟行为：当调用 transactionRepository.existsById(anyString()) 时返回 true，
        // 表示ID已存在。
        when(transactionRepository.existsById(anyString())).thenReturn(true);

        // 验证抛出DuplicateTransactionException异常
        assertThrows(DuplicateTransactionException.class, () ->
                transactionService.createTransaction(createTransactionRequest)
        );

        // 验证Mock行为：
        // 验证 transactionRepository.existsById 方法被调用了1次。
        verify(transactionRepository, times(1)).existsById(anyString());
        // 验证 transactionRepository.save 方法没有被调用。
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    /**
     * 测试成功获取单个交易的场景。
     */
    @Test
    void getTransactionById_Success() {
        // 模拟行为：当调用 transactionRepository.findById(transactionId) 时返回一个包含交易的Optional。
        when(transactionRepository.findById(transactionId)).thenReturn(transaction);

        // 执行待测试方法
        TransactionResponse response = transactionService.getTransactionById(transactionId);

        // 验证结果
        assertNotNull(response);
        assertEquals(transactionId, response.getId());
        assertEquals(transaction.getAmount(), response.getAmount());
        assertEquals(transaction.getType(), response.getType());

        // 验证Mock行为
        verify(transactionRepository, times(1)).findById(transactionId);
    }

    /**
     * 测试获取不存在的交易时抛出异常的场景。
     */
    @Test
    void getTransactionById_NotFound_ThrowsException() {
        // 模拟行为：当调用 transactionRepository.findById(anyString()) 时返回空的Optional。
        when(transactionRepository.findById(anyString())).thenReturn(null);

        // 验证抛出TransactionNotFoundException异常
        assertThrows(TransactionNotFoundException.class, () ->
                transactionService.getTransactionById("non-existent-id")
        );

        // 验证Mock行为
        verify(transactionRepository, times(1)).findById(anyString());
    }

    /**
     * 测试成功获取所有交易的场景。
     */
    @Test
    void getAllTransactions_Success() {
        // 创建多个交易实例用于测试
        Transaction transaction2 = new Transaction("id2","account001", new BigDecimal("200.00"), TransactionType.WITHDRAWAL, LocalDateTime.now(), "Test Withdrawal");
        List<Transaction> transactions = Arrays.asList(transaction, transaction2);

        // 模拟行为：当调用 transactionRepository.findAll() 时返回一个交易列表。
        when(transactionRepository.findAll()).thenReturn(transactions);

        // 执行待测试方法
        List<TransactionResponse> responses = transactionService.getAllTransactions();

        // 验证结果
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals(transaction.getId(), responses.get(0).getId());
        assertEquals(transaction2.getId(), responses.get(1).getId());

        // 验证Mock行为
        verify(transactionRepository, times(1)).findAll();
    }

    /**
     * 测试成功更新交易的场景。
     */
    @Test
    void updateTransaction_Success() {
        // 创建更新后的请求数据
        UpdateTransactionRequest updatedRequest = new UpdateTransactionRequest();
        updatedRequest.setAmount(new BigDecimal("150.00"));
        updatedRequest.setType(TransactionType.WITHDRAWAL);
        updatedRequest.setDescription("Updated Test Transaction");

        // 模拟行为：
        // 1. findById 返回现有交易
        when(transactionRepository.findById(transactionId)).thenReturn(transaction);
        // 2. update 返回更新后的交易（这里直接返回传入的existingTransaction，因为我们在Service层会修改它）
        when(transactionRepository.update(any(Transaction.class))).thenReturn(transaction); // update方法会修改传入的对象

        // 执行待测试方法
        TransactionResponse response = transactionService.updateTransaction(transactionId, updatedRequest);

        // 验证结果
        assertNotNull(response);
        assertEquals(transactionId, response.getId());
        assertEquals(updatedRequest.getAmount(), response.getAmount());
        assertEquals(updatedRequest.getType(), response.getType());
        assertEquals(updatedRequest.getDescription(), response.getDescription());

        // 验证Mock行为
        verify(transactionRepository, times(1)).findById(transactionId);
        verify(transactionRepository, times(1)).update(any(Transaction.class));
    }

    /**
     * 测试更新不存在的交易时抛出异常的场景。
     */
    @Test
    void updateTransaction_NotFound_ThrowsException() {
        // 模拟行为：findById 返回空的Optional
        when(transactionRepository.findById(anyString())).thenReturn(transaction);

        // 验证抛出TransactionNotFoundException异常
        assertThrows(TransactionNotFoundException.class, () ->
                transactionService.updateTransaction("non-existent-id", UpdateTransactionRequest)
        );

        // 验证Mock行为
        verify(transactionRepository, times(1)).findById(anyString());
        verify(transactionRepository, never()).update(any(Transaction.class)); // 确认update方法没有被调用
    }

    /**
     * 测试成功删除交易的场景。
     */
    @Test
    void deleteTransaction_Success() {
        // 模拟行为：existsById 返回 true，表示交易存在
        when(transactionRepository.existsById(transactionId)).thenReturn(true);
        // 模拟行为：deleteById 返回 true，表示删除成功
        when(transactionRepository.deleteById(transactionId)).thenReturn(true);

        // 执行待测试方法
        assertDoesNotThrow(() -> transactionService.deleteTransaction(transactionId));

        // 验证Mock行为
        verify(transactionRepository, times(1)).existsById(transactionId);
        verify(transactionRepository, times(1)).deleteById(transactionId);
    }

    /**
     * 测试删除不存在的交易时抛出异常的场景。
     */
    @Test
    void deleteTransaction_NotFound_ThrowsException() {
        // 模拟行为：existsById 返回 false，表示交易不存在
        when(transactionRepository.existsById(anyString())).thenReturn(false);

        // 验证抛出TransactionNotFoundException异常
        assertThrows(TransactionNotFoundException.class, () ->
                transactionService.deleteTransaction("non-existent-id")
        );

        // 验证Mock行为
        verify(transactionRepository, times(1)).existsById(anyString());
        verify(transactionRepository, never()).deleteById(anyString()); // 确认deleteById方法没有被调用
    }

    @Test
    void getAllTransactionsWithPagination_Success() {
        // 创建分页请求和测试数据
        Pageable pageable = mock(Pageable.class);
        Page<Transaction> transactionPage = mock(Page.class);

        // 模拟行为：当调用 transactionRepository.findAll(pageable) 时返回分页数据
        when(transactionRepository.findAll(pageable)).thenReturn(transactionPage);

        // 执行待测试方法
        Page<Transaction> result = transactionService.getAllTransactions(pageable);

        // 验证结果
        assertNotNull(result);
        assertEquals(transactionPage, result);

        // 验证Mock行为
        verify(transactionRepository, times(1)).findAll(pageable);
    }

    @Test
    void getAllTransactionsWithPagination_EmptyPage() {
        // 创建分页请求和空分页数据
        Pageable pageable = mock(Pageable.class);
        Page<Transaction> emptyPage = Page.empty();

        // 模拟行为：当调用 transactionRepository.findAll(pageable) 时返回空分页
        when(transactionRepository.findAll(pageable)).thenReturn(emptyPage);

        // 执行待测试方法
        Page<Transaction> result = transactionService.getAllTransactions(pageable);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // 验证Mock行为
        verify(transactionRepository, times(1)).findAll(pageable);
    }
}