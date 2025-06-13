package com.hsbc.transaction.service;

import com.hsbc.transaction.common.TransactionNotFoundException;
import com.hsbc.transaction.model.Transaction;
import com.hsbc.transaction.repository.TransactionRepository;
import com.hsbc.transaction.request.CreateTransactionRequest;
import com.hsbc.transaction.request.UpdateTransactionRequest;
import com.hsbc.transaction.response.TransactionResponse;
import com.hsbc.transaction.util.TokenUtil;
import com.hsbc.transaction.util.TransactionIdGenerateUtil;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * TransactionService的实现类。
 * 包含实际的业务逻辑和对Repository层的调用。
 *
 */
@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    // 通过构造函数注入TransactionRepository，这是推荐的依赖注入方式。
    public TransactionServiceImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    // 创建一个并发安全的HashMap，用于存储锁对象，键为交易ID，值为锁对象。 主要用于控制更新和删除的并发操作
    private final ConcurrentHashMap<String, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    @Override
    public TransactionResponse createTransaction(CreateTransactionRequest request) {
        // 验证防重令牌
        TokenUtil.validateAndConsumeToken(request.getPreventDuplicateToken());
        // 生成新的交易ID
        String newTransactionId = TransactionIdGenerateUtil.generateTransactionId();

        // 构建Transaction实体
        Transaction transaction = new Transaction(
                newTransactionId,
                request.getAccountId(),
                request.getAmount(),
                request.getType(),
                LocalDateTime.now(), // 设置当前时间作为交易日期
                request.getDescription()
        );

        // 保存交易
        Transaction savedTransaction = transactionRepository.save(transaction);
        // 转换为响应DTO并返回
        return TransactionResponse.fromEntity(savedTransaction);
    }

    @Override
    @Cacheable(value = "transactions", key = "#id")
    public TransactionResponse getTransactionById(String id) {
        // 根据ID查找交易，如果找不到则抛出TransactionNotFoundException
        Transaction transaction = transactionRepository.findById(id);
        if(transaction == null){
            throw new TransactionNotFoundException(String.format("交易未找到，ID: %s", id));
        }
        // 转换为响应DTO并返回
        return TransactionResponse.fromEntity(transaction);
    }

    @Override
    public List<TransactionResponse> getAllTransactions() {
        // 获取所有交易
        List<Transaction> transactionList = transactionRepository.findAll();
        if(transactionList.isEmpty()){
            return new ArrayList<>();
        }
        // 转换为响应DTO列表
        return transactionList.stream()
                .map(TransactionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public Page<TransactionResponse> getAllTransactions(Pageable pageable) {
        // 分页获取对应的交易列表
        Page<Transaction> transactionPage = transactionRepository.findAll(pageable);
        List<Transaction> content = transactionPage.getContent();
        List<TransactionResponse> result;
        if(content.isEmpty()){
            result = new ArrayList<>();
        }else {
            // 转换为响应DTO列表
            result = content.stream()
                    .map(TransactionResponse::fromEntity)
                    .toList();
        }
        return new PageImpl<>(result, pageable, transactionPage.getTotalElements());
    }

    @Override
    @CacheEvict(value = "transactions", key = "#id")
    public TransactionResponse updateTransaction(String id, UpdateTransactionRequest request) {
        ReentrantLock lock = lockMap.computeIfAbsent(id, k -> new ReentrantLock());
        lock.lock();
        try {
            // 检查交易是否存在
            Transaction existingTransaction = transactionRepository.findById(id);
            if(existingTransaction == null){
                throw new TransactionNotFoundException(String.format("无法更新，交易未找到，ID: %s", id));
            }

            // 更新交易信息
            existingTransaction.setAmount(request.getAmount());
            existingTransaction.setType(request.getType());
            existingTransaction.setDescription(request.getDescription());

            // 保存更新后的交易
            Transaction updatedTransaction = transactionRepository.update(existingTransaction);
            // 转换为响应DTO并返回
            return TransactionResponse.fromEntity(updatedTransaction);
        } finally {
            lock.unlock();
        }
    }

    @Override
    @CacheEvict(value = "transactions", key = "#id")
    public void deleteTransaction(String id) {
        ReentrantLock lock = lockMap.computeIfAbsent(id, k -> new ReentrantLock());
        lock.lock();
        try {
            // 检查交易是否存在，如果不存在则抛出异常
            if (!transactionRepository.existsById(id)) {
                throw new TransactionNotFoundException((String.format("无法删除，交易未找到，ID: %s", id)));
            }
            // 删除交易
            transactionRepository.deleteById(id);
        } finally {
            lock.unlock();
        }
    }
}
