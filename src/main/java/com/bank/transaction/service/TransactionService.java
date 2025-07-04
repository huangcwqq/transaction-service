package com.bank.transaction.service;

import com.bank.transaction.request.CreateTransactionRequest;
import com.bank.transaction.request.UpdateTransactionRequest;
import com.bank.transaction.response.TransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 交易业务逻辑层接口。
 * 定义了业务操作，如创建、查询、更新、删除交易。
 */
public interface TransactionService {

    /**
     * 创建新交易。
     * 如果请求中提供了 ID 且该 ID 已存在，则抛出 DuplicateTransactionException。
     * @param request 交易请求数据传输对象
     * @return 创建后的交易实体
     *
     */
    TransactionResponse createTransaction(CreateTransactionRequest request);

    /**
     * 根据交易ID获取交易详情。
     * @param id 交易ID
     * @return 交易响应DTO
     */
    TransactionResponse getTransactionById(String id);

    /**
     * 获取所有交易。
     * @return 所有交易响应DTO的列表
     */
    List<TransactionResponse> getAllTransactions();

    /**
     * 分页获取所有交易。
     * @param pageable 分页信息
     * @return 包含交易的分页结果
     */
     Page<TransactionResponse> getAllTransactions(Pageable pageable);

    /**
     * 更新一笔现有交易。
     * @param id 要更新的交易ID
     * @param request 包含更新数据的请求DTO
     * @return 更新成功的交易响应DTO
     */
    TransactionResponse updateTransaction(String id, UpdateTransactionRequest request);

    /**
     * 根据交易ID删除交易。
     * @param id 交易ID
     */
    void deleteTransaction(String id);
}
