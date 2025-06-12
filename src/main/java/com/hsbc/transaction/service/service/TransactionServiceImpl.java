package com.hsbc.transaction.service.service;

import com.hsbc.transaction.service.common.DuplicateTransactionException;
import com.hsbc.transaction.service.common.TransactionNotFoundException;
import com.hsbc.transaction.service.model.Transaction;
import com.hsbc.transaction.service.repository.TransactionRepository;
import com.hsbc.transaction.service.request.TransactionRequest;
import com.hsbc.transaction.service.response.TransactionResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * TransactionService的实现类。
 * 包含实际的业务逻辑和对Repository层的调用。
 * @Service 注解表示这是一个Spring管理的业务服务组件。
 */
@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    // 通过构造函数注入TransactionRepository，这是推荐的依赖注入方式。
    public TransactionServiceImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public TransactionResponse createTransaction(TransactionRequest request) {
        // 生成唯一的交易ID
        String newId = UUID.randomUUID().toString();

        // 检查ID是否意外重复（尽管UUID冲突概率极低，但作为严谨性检查）
        if (transactionRepository.existsById(newId)) {
            throw new DuplicateTransactionException("交易ID已存在: " + newId);
        }

        // 构建Transaction实体
        Transaction transaction = new Transaction(
                newId,
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
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException("交易未找到，ID: " + id));
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

    @Override
    public TransactionResponse updateTransaction(String id, TransactionRequest request) {
        // 检查交易是否存在
        Transaction existingTransaction = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException("无法更新，交易未找到，ID: " + id));

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
            throw new TransactionNotFoundException("无法删除，交易未找到，ID: " + id);
        }
        // 删除交易
        transactionRepository.deleteById(id);
    }
}
