package com.hsbc.transaction.service.service;

import com.hsbc.transaction.service.request.TransactionRequest;
import com.hsbc.transaction.service.response.TransactionResponse;

import java.util.List;

/**
 * 交易业务逻辑层接口。
 * 定义了业务操作，如创建、查询、更新、删除交易。
 */
public interface TransactionService {

    /**
     * 创建一笔新交易。
     * @param request 包含交易数据的请求DTO
     * @return 创建成功的交易响应DTO
     */
    TransactionResponse createTransaction(TransactionRequest request);

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
     * 更新一笔现有交易。
     * @param id 要更新的交易ID
     * @param request 包含更新数据的请求DTO
     * @return 更新成功的交易响应DTO
     */
    TransactionResponse updateTransaction(String id, TransactionRequest request);

    /**
     * 根据交易ID删除交易。
     * @param id 交易ID
     */
    void deleteTransaction(String id);
}
