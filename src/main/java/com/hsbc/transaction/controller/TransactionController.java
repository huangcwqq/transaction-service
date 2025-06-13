package com.hsbc.transaction.controller;

import com.hsbc.transaction.common.RestResponse;
import com.hsbc.transaction.model.Transaction;
import com.hsbc.transaction.request.CreateTransactionRequest;
import com.hsbc.transaction.request.UpdateTransactionRequest;
import com.hsbc.transaction.response.TransactionResponse;
import com.hsbc.transaction.service.TransactionService;
import com.hsbc.transaction.util.TokenUtil;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 交易管理RestFul API控制器。
 * 处理所有与交易相关的HTTP请求。
 *
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    // 通过构造函数注入TransactionService，更安全
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * 生成防重token返回给前端入参调用
     */
    @PostMapping
    public RestResponse<String> generateToken() {
        return RestResponse.ok(TokenUtil.generateToken());
    }

    /**
     * 创建一笔新交易。
     * HTTP Method: POST
     * URL: /api/transactions
     *
     */
    @PostMapping
    public RestResponse<TransactionResponse> createTransaction(@Valid @RequestBody CreateTransactionRequest request) {
        TransactionResponse response = transactionService.createTransaction(request);
        return RestResponse.ok(response);
    }

    /**
     * 根据ID获取特定交易详情。
     * HTTP Method: GET
     * URL: /api/transactions/{id}
     * @param id 路径变量中的交易ID。
     * @return 交易响应DTO和200 OK状态码。如果未找到，GlobalExceptionHandler会返回404。
     */
    @GetMapping("/{id}")
    public RestResponse<TransactionResponse> getTransactionById(@PathVariable String id) {
        TransactionResponse response = transactionService.getTransactionById(id);
        return RestResponse.ok(response);
    }

    /**
     * 获取所有交易。
     * HTTP Method: GET
     * URL: /api/transactions
     * @return 所有交易响应DTO的列表和200 OK状态码。
     */
    @GetMapping
    public RestResponse<List<TransactionResponse>> getAllTransactions() {
        List<TransactionResponse> transactions = transactionService.getAllTransactions();
        return RestResponse.ok(transactions);
    }

    /**
     * 更新一笔现有交易。
     * HTTP Method: PUT
     * URL: /api/transactions/{id}
     * @param id 路径变量中的交易ID。
     * @param request 包含更新数据的请求体，通过@Valid进行数据验证。
     * @return 更新后的交易响应DTO和200 OK状态码。如果未找到，GlobalExceptionHandler会返回404。
     */
    @PutMapping("/{id}")
    public RestResponse<TransactionResponse> updateTransaction(@PathVariable String id, @Valid @RequestBody UpdateTransactionRequest request) {
        TransactionResponse response = transactionService.updateTransaction(id, request);
        return RestResponse.ok(response);
    }

    /**
     * 根据ID删除交易。
     * HTTP Method: DELETE
     * URL: /api/transactions/{id}
     * @param id 路径变量中的交易ID。
     * @return 204 No Content状态码。如果未找到，GlobalExceptionHandler会返回404。
     */
    @DeleteMapping("/{id}")
    public RestResponse<Void> deleteTransaction(@PathVariable String id) {
        transactionService.deleteTransaction(id);
        return RestResponse.ok();
    }

    /**
     * 分页获取所有交易。
     * @param page 页码 (默认 0)
     * @param size 每页大小 (默认 10)
     * @return 包含交易列表的分页响应对象和 200 OK 状态码
     */
    @GetMapping("/page")
    public RestResponse<Page<TransactionResponse>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> transactionPage = transactionService.getAllTransactions(pageable);

        List<TransactionResponse> content = transactionPage.getContent().stream()
                .map(TransactionResponse::fromEntity)
                .collect(Collectors.toList());

        return RestResponse.ok(new PageImpl<>(content, pageable, transactionPage.getTotalElements()));
    }
}

