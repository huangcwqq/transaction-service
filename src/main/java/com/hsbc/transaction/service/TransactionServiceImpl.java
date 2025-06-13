package com.hsbc.transaction.service;

import com.hsbc.transaction.common.TransactionErrors;
import com.hsbc.transaction.model.Transaction;
import com.hsbc.transaction.repository.TransactionRepository;
import com.hsbc.transaction.request.CreateTransactionRequest;
import com.hsbc.transaction.request.UpdateTransactionRequest;
import com.hsbc.transaction.response.TransactionResponse;
import com.hsbc.transaction.util.TokenUtil;
import com.hsbc.transaction.util.TransactionIdGenerateUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
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

    @Override
    public TransactionResponse createTransaction(CreateTransactionRequest request) {
        TokenUtil.validateAndConsumeToken(request.getPreventDuplicateToken());

        String newTransactionId = TransactionIdGenerateUtil.generateTransactionId();

        // 检查交易 ID 是否已存在，防止重复创建
        if (transactionRepository.existsById(newTransactionId)) {
            throw TransactionErrors.repeatTransaction(String.format("重复的交易ID: %s", newTransactionId));
        }

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
    public TransactionResponse getTransactionById(String id) {
        // 根据ID查找交易，如果找不到则抛出TransactionNotFoundException
        Transaction transaction = transactionRepository.findById(id);
        if(transaction == null){
            throw TransactionErrors.transactionNotFound(String.format("交易未找到，ID: %s", id));
        }
        // 转换为响应DTO并返回
        return TransactionResponse.fromEntity(transaction);
    }

    @Override
    public List<TransactionResponse> getAllTransactions() {
        // 获取所有交易并转换为响应DTO列表
        return transactionRepository.findAll().stream()
                .map(TransactionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public Page<Transaction> getAllTransactions(Pageable pageable) {
        return transactionRepository.findAll(pageable);
    }

    @Override
    public TransactionResponse updateTransaction(String id, UpdateTransactionRequest request) {
        // 检查交易是否存在
        Transaction existingTransaction = transactionRepository.findById(id);
        if(existingTransaction == null){
            throw TransactionErrors.transactionNotFound(String.format("无法更新，交易未找到，ID: %s", id));
        }

        // 更新交易信息
        existingTransaction.setAmount(request.getAmount());
        existingTransaction.setType(request.getType());
        existingTransaction.setDescription(request.getDescription());
        // 日期通常不更新，或者根据业务需求更新为当前时间
        // existingTransaction.setDate(LocalDateTime.now());

        // 保存更新后的交易
        Transaction updatedTransaction = transactionRepository.update(existingTransaction);
        // 转换为响应DTO并返回
        return TransactionResponse.fromEntity(updatedTransaction);
    }

    @Override
    public void deleteTransaction(String id) {
        // 检查交易是否存在，如果不存在则抛出异常
        if (!transactionRepository.existsById(id)) {
            throw TransactionErrors.transactionNotFound((String.format("无法删除，交易未找到，ID: ", id)));
        }
        // 删除交易
        transactionRepository.deleteById(id);
    }
}
