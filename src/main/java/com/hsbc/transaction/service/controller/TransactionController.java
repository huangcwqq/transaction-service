package com.hsbc.transaction.service.controller;

import com.hsbc.transaction.service.request.TransactionRequest;
import com.hsbc.transaction.service.response.TransactionResponse;
import com.hsbc.transaction.service.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 交易管理RESTful API控制器。
 * 处理所有与交易相关的HTTP请求。
 * @RestController = @Controller + @ResponseBody，表示所有方法返回的数据直接作为HTTP响应体。
 * @RequestMapping("/api/transactions") 定义了所有HTTP请求的基础路径。
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    // 通过构造函数注入TransactionService
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * 创建一笔新交易。
     * HTTP Method: POST
     * URL: /api/transactions
     * @param request 包含交易数据的请求体，通过@Valid进行数据验证。
     * @return 创建成功的交易响应DTO和201 Created状态码。
     */
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@Valid @RequestBody TransactionRequest request) {
        TransactionResponse response = transactionService.createTransaction(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * 根据ID获取特定交易详情。
     * HTTP Method: GET
     * URL: /api/transactions/{id}
     * @param id 路径变量中的交易ID。
     * @return 交易响应DTO和200 OK状态码。如果未找到，GlobalExceptionHandler会返回404。
     */
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransactionById(@PathVariable String id) {
        TransactionResponse response = transactionService.getTransactionById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取所有交易。
     * HTTP Method: GET
     * URL: /api/transactions
     * @return 所有交易响应DTO的列表和200 OK状态码。
     */
    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getAllTransactions() {
        List<TransactionResponse> transactions = transactionService.getAllTransactions();
        return ResponseEntity.ok(transactions);
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
    public ResponseEntity<TransactionResponse> updateTransaction(@PathVariable String id, @Valid @RequestBody TransactionRequest request) {
        TransactionResponse response = transactionService.updateTransaction(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 根据ID删除交易。
     * HTTP Method: DELETE
     * URL: /api/transactions/{id}
     * @param id 路径变量中的交易ID。
     * @return 204 No Content状态码。如果未找到，GlobalExceptionHandler会返回404。
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable String id) {
        transactionService.deleteTransaction(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content 表示请求成功但没有返回数据。
    }
}

