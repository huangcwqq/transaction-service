package com.bank.transaction.repository;

import com.bank.transaction.enums.TransactionType;
import com.bank.transaction.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InMemoryTransactionRepositoryTest {

    @InjectMocks
    private InMemoryTransactionRepository repository;

    // 公共测试数据
    private Transaction transaction1;
    private Transaction transaction2;
    private Transaction transaction3;
    private Transaction transaction4;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        BigDecimal amount1 = new BigDecimal("100.00");
        BigDecimal amount2 = new BigDecimal("200.00");
        BigDecimal amount3 = new BigDecimal("300.00");
        BigDecimal amount4 = new BigDecimal("400.00");

        transaction1 = new Transaction("TX123", "ACC123", amount1, TransactionType.DEPOSIT, now, "Deposit 1");
        transaction2 = new Transaction("TX456", "ACC456", amount2, TransactionType.WITHDRAWAL, now, "Withdrawal 1");
        transaction3 = new Transaction("TX789", "ACC789", amount3, TransactionType.DEPOSIT, now.minusDays(1), "Deposit 2");
        transaction4 = new Transaction("TX999", "ACC999", amount4, TransactionType.WITHDRAWAL, now.minusDays(2), "Withdrawal 2");
    }

    /**
     * TC01: 测试正常保存一个合法的 Transaction 对象
     */
    @Test
    void testSave_ShouldAddTransactionToMapAndReturnIt() {
        // Arrange
        String id = "TX123";
        LocalDateTime now = LocalDateTime.now();
        BigDecimal amount = new BigDecimal("100.00");
        Transaction transaction = new Transaction(id, "ACC123", amount, TransactionType.DEPOSIT, now, "Deposit");

        // Act
        Transaction result = repository.save(transaction);

        // Assert
        assertNotNull(result);
        assertEquals(transaction, result);
        assertEquals(transaction, repository.findById(id));
    }

    /**
     * TC02: 测试传入 null 时应抛出 NullPointerException
     */
    @Test
    void testSave_WhenTransactionIsNull_ShouldThrowNullPointerException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> repository.save(null));
    }

    /**
     * TC03: 测试传入的 Transaction ID 为 null 时应抛出 NullPointerException
     */
    @Test
    void testSave_WhenIdIsNull_ShouldThrowNullPointerException() {
        // Arrange
        Transaction transaction = new Transaction();
        transaction.setId(null); // 设置为 null

        // Act & Assert
        assertThrows(NullPointerException.class, () -> repository.save(transaction));
    }

    /**
     * TC04: 测试相同 ID 的多次保存是否会覆盖旧值
     */
    @Test
    void testSave_SameIdMultipleTimes_ShouldOverridePreviousValue() {
        // Arrange
        String id = "TX456";
        Transaction first = new Transaction(id, "ACC789", new BigDecimal("100"), TransactionType.DEPOSIT, LocalDateTime.now(), "First");
        Transaction second = new Transaction(id, "ACC789", new BigDecimal("200"), TransactionType.WITHDRAWAL, LocalDateTime.now(), "Second");

        // Act
        repository.save(first);
        repository.save(second);

        // Assert
        assertEquals(second, repository.findById(id));
    }

    @Test
    void testFindById_WhenIdExists_ShouldReturnTransaction() {
        // Arrange
        String id = "TX123";
        LocalDateTime now = LocalDateTime.now();
        BigDecimal amount = new BigDecimal("100.00");
        Transaction expected = new Transaction(id, "ACC123", amount, TransactionType.DEPOSIT, now, "Deposit");
        repository.save(expected);

        // Act
        Transaction actual = repository.findById(id);

        // Assert
        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    @Test
    void testFindById_WhenIdDoesNotExist_ShouldReturnNull() {
        // Arrange
        String id = "TX123";
        LocalDateTime now = LocalDateTime.now();
        BigDecimal amount = new BigDecimal("100.00");
        Transaction expected = new Transaction(id, "ACC123", amount, TransactionType.DEPOSIT, now, "Deposit");
        repository.save(expected);

        // Act
        String nonExistentId = "tx2";
        Transaction result = repository.findById(nonExistentId);

        // Assert
        assertNull(result);
    }

    @Test
    void testFindById_WhenIdIsNull_ShouldReturnNullIfNotPresent() {
        // Act and Assert
        assertThrows(NullPointerException.class, () -> repository.findById(null));
    }

    /**
     * TC05: 测试当没有保存任何交易时，findAll() 应返回空列表
     */
    @Test
    void testFindAll_WhenNoTransactions_ShouldReturnEmptyList() {
        // Act
        List<Transaction> result = repository.findAll();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty(), "Expected empty list when no transactions are stored.");
    }

    /**
     * TC06: 测试保存一个交易后，findAll() 是否能正确返回该交易
     */
    @Test
    void testFindAll_WhenOneTransactionExists_ShouldReturnSingleElementList() {
        // Arrange
        repository.save(transaction1);

        // Act
        List<Transaction> result = repository.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(transaction1));
    }

    /**
     * TC07: 测试保存多个交易后，findAll() 是否能正确返回所有交易
     */
    @Test
    void testFindAll_WhenMultipleTransactionsExist_ShouldReturnAllTransactions() {
        // Arrange
        repository.save(transaction1);
        repository.save(transaction2);

        // Act
        List<Transaction> result = repository.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(transaction1));
        assertTrue(result.contains(transaction2));
    }

    /**
     * TC08: 测试多次调用 findAll() 是否每次都返回当前最新的交易列表
     */
    @Test
    void testFindAll_CalledMultipleTimes_ShouldReflectCurrentState() {
        // First call - empty
        List<Transaction> firstCall = repository.findAll();
        assertTrue(firstCall.isEmpty());

        // Add one transaction
        repository.save(transaction1);
        List<Transaction> secondCall = repository.findAll();
        assertEquals(1, secondCall.size());
        assertTrue(secondCall.contains(transaction1));

        // Add another transaction
        repository.save(transaction2);
        List<Transaction> thirdCall = repository.findAll();
        assertEquals(2, thirdCall.size());
        assertTrue(thirdCall.contains(transaction1));
        assertTrue(thirdCall.contains(transaction2));
    }

    /**
     * TC01: 测试正常分页 - 获取第一页，每页2条记录
     */
    @Test
    void testFindAll_NormalPaging_FirstPage_ShouldReturnFirstTwoRecords() {
        // 初始化存储
        repository.save(transaction1);
        repository.save(transaction2);
        repository.save(transaction3);
        repository.save(transaction4);
        // Arrange
        Pageable pageable = PageRequest.of(0, 2);

        // Act
        Page<Transaction> result = repository.findAll(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(transaction1.getId(), result.getContent().get(0).getId()); // 最新的在最前面
        assertEquals(transaction2.getId(), result.getContent().get(1).getId());
        assertEquals(4, result.getTotalElements());
        assertTrue(result.getTotalPages() >= 2);
    }

    /**
     * TC02: 测试 pageable 为 null 时应抛出 NullPointerException
     */
    @Test
    void testFindAll_WhenPageableIsNull_ShouldThrowNullPointerException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> repository.findAll(null));
    }

    /**
     * TC03: 测试空数据集时返回空页面
     */
    @Test
    void testFindAll_EmptyDataStore_ShouldReturnEmptyPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 2);

        // Act
        Page<Transaction> result = repository.findAll(pageable);

        // Assert
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getTotalPages());
    }

    /**
     * TC04: 测试第二页，每页2条记录
     */
    @Test
    void testFindAll_SecondPage_TwoItemsPerPage_ShouldReturnThirdAndFourthRecords() {
        // 初始化存储
        repository.save(transaction1);
        repository.save(transaction2);
        repository.save(transaction3);
        repository.save(transaction4);
        // Arrange
        Pageable pageable = PageRequest.of(1, 2);

        // Act
        Page<Transaction> result = repository.findAll(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(transaction3.getId(), result.getContent().get(0).getId());
        assertEquals(transaction4.getId(), result.getContent().get(1).getId());
        assertEquals(4, result.getTotalElements());
    }

    /**
     * TC05: 测试请求超出范围的页码，应返回空列表
     */
    @Test
    void testFindAll_PageNumberExceedsTotal_ShouldReturnEmptyList() {
        // 初始化存储
        repository.save(transaction1);
        repository.save(transaction2);
        repository.save(transaction3);
        repository.save(transaction4);
        // Arrange
        Pageable pageable = PageRequest.of(10, 2); // 第10页，每页2条

        // Act
        Page<Transaction> result = repository.findAll(pageable);

        // Assert
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(4, result.getTotalElements());
    }

    /**
     * TC06: 测试单页能容纳所有记录的情况
     */
    @Test
    void testFindAll_OnePageWithSizeEqualToTotal_ShouldReturnAllRecords() {
        // 初始化存储
        repository.save(transaction1);
        repository.save(transaction2);
        repository.save(transaction3);
        repository.save(transaction4);
        // Arrange
        Pageable pageable = PageRequest.of(0, 4); // 一页显示所有记录

        // Act
        Page<Transaction> result = repository.findAll(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(4, result.getContent().size());
        List<String> expectedIds = List.of(
                transaction1.getId(),
                transaction2.getId(),
                transaction3.getId(),
                transaction4.getId()
        );
        List<String> actualIds = result.getContent().stream()
                .map(Transaction::getId)
                .toList();

        assertEquals(expectedIds, actualIds);
    }

    /**
     * TC07: 测试分页边界情况 - 偏移量刚好在列表末尾
     */
    @Test
    void testFindAll_OffsetAtEndOfList_ShouldReturnEmptyList() {
        // 初始化存储
        repository.save(transaction1);
        repository.save(transaction2);
        repository.save(transaction3);
        repository.save(transaction4);
        // Arrange
        Pageable pageable = PageRequest.of(0, 4); // 获取全部
        Page<Transaction> fullResult = repository.findAll(pageable);
        int totalSize = (int) fullResult.getTotalElements();

        pageable = PageRequest.of(totalSize, 2); // 偏移量等于列表长度

        // Act
        Page<Transaction> result = repository.findAll(pageable);

        // Assert
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
    }

    /**
     * * TC01: 测试当 transaction ID 存在时，update 应成功更新并返回该 transaction
     */
    @Test
    void testUpdate_WhenIdExists_ShouldReplaceAndReturnUpdatedTransaction() {
        // Arrange
        String id = "TX100";
        Transaction oldTx = new Transaction(id, "ACC100", new BigDecimal("100"), TransactionType.DEPOSIT, LocalDateTime.now(), "Old");
        Transaction newTx = new Transaction(id, "ACC999", new BigDecimal("200"), TransactionType.WITHDRAWAL, LocalDateTime.now(), "New");
        // 先插入旧数据
        repository.save(oldTx);

        // Act
        Transaction result = repository.update(newTx);

        // Assert
        assertNotNull(result);
        assertEquals(newTx, result);
        assertEquals(newTx, repository.findById(id));
    }

    /**
     * TC02: 测试当 transaction ID 不存在时，update 应返回 null
     */
    @Test
    void testUpdate_WhenIdNotExists_ShouldReturnNull() {
        // Arrange
        String id = "TX200";
        Transaction tx = new Transaction(id, "ACC200", new BigDecimal("300"), TransactionType.DEPOSIT, LocalDateTime.now(), "Test");

        // Act
        Transaction result = repository.update(tx);

        // Assert
        assertNull(result);
    }

    /**
     * TC03: 测试当传入 transaction 为 null 时应抛出 NullPointerException
     */
    @Test
    void testUpdate_WhenTransactionIsNull_ShouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> repository.update(null));
    }

    /**
     * TC04: 测试当 transaction ID 为 null 时应抛出 NullPointerException
     */
    @Test
    void testUpdate_WhenIdIsNull_ShouldThrowNullPointerException() {
        // Arrange
        Transaction tx = mock(Transaction.class);
        // 模拟 getId() 返回 null
        when(tx.getId()).thenReturn(null);

        assertThrows(NullPointerException.class, () -> repository.update(tx));
    }

    /**
     * TC05: 测试 deleteById 当 ID 存在时应成功删除并返回 true
     */
    @Test
    void testDeleteById_WhenIdExists_ShouldReturnTrueAndRemoveFromMap() {
        // Arrange
        String id = "TX123";
        repository.save(transaction1); // 先保存一个交易

        // Act
        boolean result = repository.deleteById(id);

        // Assert
        assertTrue(result);
        assertFalse(repository.existsById(id)); // 确保已删除
    }

    /**
     * TC06: 测试 deleteById 当 ID 不存在时应返回 false
     */
    @Test
    void testDeleteById_WhenIdNotExists_ShouldReturnFalse() {
        // Arrange
        String id = "NON_EXISTENT_ID";

        // Act
        boolean result = repository.deleteById(id);

        // Assert
        assertFalse(result);
    }

    /**
     * TC07: 测试 deleteById 当传入 null 应抛出 NullPointerException
     */
    @Test
    void testDeleteById_WhenIdIsNull_ShouldThrowNullPointerException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> repository.deleteById(null));
    }

    /**
     * TC08: 测试 existsById 当 ID 存在时应返回 true
     */
    @Test
    void testExistsById_WhenIdExists_ShouldReturnTrue() {
        // Arrange
        String id = "TX123";
        repository.save(transaction1); // 先保存一个交易

        // Act
        boolean result = repository.existsById(id);

        // Assert
        assertTrue(result);
    }

    /**
     * TC09: 测试 existsById 当 ID 不存在时应返回 false
     */
    @Test
    void testExistsById_WhenIdNotExists_ShouldReturnFalse() {
        // Arrange
        String id = "NON_EXISTENT_ID";

        // Act
        boolean result = repository.existsById(id);

        // Assert
        assertFalse(result);
    }

    /**
     * TC10: 测试 existsById 当传入 null 应抛出 NullPointerException
     */
    @Test
    void testExistsById_WhenIdIsNull_ShouldThrowNullPointerException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> repository.existsById(null));
    }
}