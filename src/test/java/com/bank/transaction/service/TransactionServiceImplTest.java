package com.bank.transaction.service;

import com.bank.transaction.common.InvalidRequestException;
import com.bank.transaction.common.TransactionNotFoundException;
import com.bank.transaction.enums.TransactionType;
import com.bank.transaction.model.Transaction;
import com.bank.transaction.repository.TransactionRepository;
import com.bank.transaction.request.CreateTransactionRequest;
import com.bank.transaction.request.UpdateTransactionRequest;
import com.bank.transaction.response.TransactionResponse;
import com.bank.transaction.util.TokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * TransactionService的单元测试类。
 * 使用MockitoExtension启用Mockito注解，用于模拟依赖。
 */
@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    private TransactionRepository transactionRepository;
    private TransactionServiceImpl transactionService;

    @BeforeEach
    void setUp() {
        transactionRepository = Mockito.mock(TransactionRepository.class);
        transactionService = new TransactionServiceImpl(transactionRepository);
    }

    @Test
    void testCreateTransaction_Success(){
        // 准备测试数据
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setPreventDuplicateToken("TOKEN123");
        request.setAccountId("ACC123");
        request.setAmount(new BigDecimal("100.00"));
        request.setDescription("存款业务");
        request.setPreventDuplicateToken("TOKEN789");
        request.setType(TransactionType.DEPOSIT);

        // 模拟 TokenUtil.validateAndConsumeToken 不抛出异常
        try (MockedStatic<TokenUtil> mockedTokenUtil = Mockito.mockStatic(TokenUtil.class)) {
            mockedTokenUtil.when(() -> TokenUtil.validateAndConsumeToken("TOKEN123")).thenAnswer(invocation -> null);

            // 构建模拟的 Transaction 对象
            Transaction expectedTransaction = new Transaction(
                    "TRANS123",
                    "ACC123",
                    new BigDecimal("100.00"),
                    TransactionType.DEPOSIT,
                    LocalDateTime.now(),
                    "存款"
            );

            when(transactionRepository.save(any(Transaction.class))).thenReturn(expectedTransaction);

            // 执行测试
            TransactionResponse response = transactionService.createTransaction(request);

            // 验证结果
            assertNotNull(response);
            assertEquals("TRANS123", response.getId());
            assertEquals("ACC123", response.getAccountId());
            assertEquals(new BigDecimal("100.00"), response.getAmount());
            assertEquals(TransactionType.DEPOSIT, response.getType());
            assertEquals("存款", response.getDescription());
        }
    }

    @Test
    void testCreateTransaction_EmptyToken() {
        // 准备测试数据
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAccountId("ACC123");
        request.setAmount(new BigDecimal("100.00"));
        request.setDescription("存款");
        request.setPreventDuplicateToken(null);  // 空 token
        request.setType(TransactionType.DEPOSIT);

        // 验证异常
        assertThrows(InvalidRequestException.class, () -> transactionService.createTransaction(request));
    }

    @Test
    void testGetTransactionById_Exists() {
        // 准备测试数据
        String transactionId = "TRANS123";

        Transaction transaction = new Transaction(
                transactionId,
                "ACC123",
                new BigDecimal("100.00"),
                TransactionType.DEPOSIT,
                LocalDateTime.now(),
                "存款"
        );

        when(transactionRepository.findById(transactionId)).thenReturn(transaction);

        // 执行测试
        TransactionResponse response = transactionService.getTransactionById(transactionId);

        // 验证结果
        assertNotNull(response);
        assertEquals(transactionId, response.getId());
        assertEquals("ACC123", response.getAccountId());
        assertEquals(new BigDecimal("100.00"), response.getAmount());
        assertEquals(TransactionType.DEPOSIT, response.getType());
        assertEquals("存款", response.getDescription());
    }

    @Test
    void testGetTransactionById_NotExists() {
        // 准备测试数据
        String transactionId = "NON_EXISTENT";

        when(transactionRepository.findById(transactionId)).thenReturn(null);

        // 验证异常
        assertThrows(TransactionNotFoundException.class, () -> transactionService.getTransactionById(transactionId));
    }

    @Test
    void testGetAllTransactions_NonEmpty() {
        // 构建测试数据
        List<Transaction> transactions = new ArrayList<>();

        transactions.add(new Transaction(
                "TRANS123",
                "ACC123",
                new BigDecimal("100.00"),
                TransactionType.DEPOSIT,
                LocalDateTime.now(),
                "存款"
        ));

        transactions.add(new Transaction(
                "TRANS456",
                "ACC456",
                new BigDecimal("200.00"),
                TransactionType.WITHDRAWAL,
                LocalDateTime.now().minusDays(1),
                "取款"
        ));

        when(transactionRepository.findAll()).thenReturn(transactions);

        // 执行测试
        List<TransactionResponse> responses = transactionService.getAllTransactions();

        // 验证结果
        assertNotNull(responses);
        assertEquals(2, responses.size());

        assertEquals("TRANS123", responses.getFirst().getId());
        assertEquals("ACC123", responses.getFirst().getAccountId());
        assertEquals(new BigDecimal("100.00"), responses.getFirst().getAmount());
        assertEquals(TransactionType.DEPOSIT, responses.getFirst().getType());
        assertEquals("存款", responses.getFirst().getDescription());

        assertEquals("TRANS456", responses.get(1).getId());
        assertEquals("ACC456", responses.get(1).getAccountId());
        assertEquals(new BigDecimal("200.00"), responses.get(1).getAmount());
        assertEquals(TransactionType.WITHDRAWAL, responses.get(1).getType());
        assertEquals("取款", responses.get(1).getDescription());
    }

    @Test
    void testGetAllTransactions_Empty() {
        when(transactionRepository.findAll()).thenReturn(new ArrayList<>());

        // 执行测试
        List<TransactionResponse> responses = transactionService.getAllTransactions();

        // 验证结果
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    /**
     * 测试场景：repository 返回空数据
     */
    @Test
    void testGetAllTransactions_EmptyResult() {
        // Arrange
        Pageable pageable = mock(Pageable.class);
        Page<Transaction> emptyPage = new PageImpl<>(List.of());

        when(transactionRepository.findAll(pageable)).thenReturn(emptyPage);

        // Act
        var result = transactionService.getAllTransactions(pageable);

        // Assert
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
        verify(transactionRepository, times(1)).findAll(pageable);
    }

    /**
     * 测试场景：repository 返回一条交易数据
     */
    @Test
    void testGetAllTransactions_WithData() {
        // Arrange
        Pageable pageable = mock(Pageable.class);
        LocalDateTime now = LocalDateTime.now();

        Transaction transaction = Transaction.builder()
                .id("1")
                .accountId("acc123")
                .amount(BigDecimal.valueOf(100))
                .type(TransactionType.DEPOSIT)
                .date(now)
                .description("Test transaction")
                .build();

        Page<Transaction> page = new PageImpl<>(List.of(transaction));

        when(transactionRepository.findAll(pageable)).thenReturn(page);

        // Act
        var result = transactionService.getAllTransactions(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        TransactionResponse response = result.getContent().getFirst();
        assertEquals("1", response.getId());
        assertEquals("acc123", response.getAccountId());
        assertEquals(BigDecimal.valueOf(100), response.getAmount());
        assertEquals(TransactionType.DEPOSIT, response.getType());
        assertEquals(now, response.getDate());
        assertEquals("Test transaction", response.getDescription());

        verify(transactionRepository, times(1)).findAll(pageable);
    }

    @Test
    void testUpdateTransaction_Success() {
        // 准备测试数据
        String transactionId = "TRANS123";

        Transaction existingTransaction = new Transaction(
                transactionId,
                "ACC123",
                new BigDecimal("100.00"),
                TransactionType.DEPOSIT,
                LocalDateTime.now(),
                "存款"
        );

        UpdateTransactionRequest request = new UpdateTransactionRequest();
        request.setAmount(new BigDecimal("150.00"));
        request.setType(TransactionType.WITHDRAWAL);
        request.setDescription("修改后的描述");

        when(transactionRepository.findById(transactionId)).thenReturn(existingTransaction);
        when(transactionRepository.update(any(Transaction.class))).thenReturn(existingTransaction);

        // 执行测试
        TransactionResponse response = transactionService.updateTransaction(transactionId, request);

        // 验证结果
        assertNotNull(response);
        assertEquals(transactionId, response.getId());
        assertEquals("ACC123", response.getAccountId());
        assertEquals(new BigDecimal("150.00"), response.getAmount());
        assertEquals(TransactionType.WITHDRAWAL, response.getType());
        assertEquals("修改后的描述", response.getDescription());
    }

    @Test
    void testUpdateTransaction_NotExists() {
        // 准备测试数据
        String transactionId = "NON_EXISTENT";

        UpdateTransactionRequest request = new UpdateTransactionRequest();
        request.setAmount(new BigDecimal("150.00"));
        request.setType(TransactionType.WITHDRAWAL);
        request.setDescription("修改后的描述");

        when(transactionRepository.findById(transactionId)).thenReturn(null);

        // 验证异常
        assertThrows(TransactionNotFoundException.class, () -> transactionService.updateTransaction(transactionId, request));
    }

    @Test
    void testDeleteTransaction_Success() {
        // 准备测试数据
        String transactionId = "TRANS123";

        when(transactionRepository.existsById(transactionId)).thenReturn(true);

        doReturn(true).when(transactionRepository).deleteById(transactionId);

        // 执行测试
        assertDoesNotThrow(() -> transactionService.deleteTransaction(transactionId));

        // 验证交互
        verify(transactionRepository, times(1)).existsById(transactionId);
        verify(transactionRepository, times(1)).deleteById(transactionId);
    }

    @Test
    void testDeleteTransaction_NotExists() {
        // 准备测试数据
        String transactionId = "NON_EXISTENT";

        when(transactionRepository.existsById(transactionId)).thenReturn(false);

        // 验证异常
        assertThrows(TransactionNotFoundException.class, () -> transactionService.deleteTransaction(transactionId));

        // 验证交互
        verify(transactionRepository, times(1)).existsById(transactionId);
        verify(transactionRepository, never()).deleteById(transactionId);
    }
}

