package com.bank.transaction.controller;

import com.bank.transaction.request.CreateTransactionRequest;
import com.bank.transaction.request.UpdateTransactionRequest;
import com.bank.transaction.response.TransactionResponse;
import com.bank.transaction.service.TransactionService;
import com.bank.transaction.util.TokenUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 交易管理RestFul API控制器。
 * 处理所有与交易相关的HTTP请求。
 *
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
     * 获取访问令牌。
     * @return 生成的访问令牌和200 OK状态码。
     */
    @Operation(summary = "获取访问令牌", description = "生成并返回访问令牌")
    @GetMapping("/token")
    public ResponseEntity<String> obtainToken() {
        return ResponseEntity.ok(TokenUtil.generateToken());
    }

    /**
     * 创建一笔新交易。
     * @param request 包含交易数据的请求体，通过@Valid进行数据验证。
     * @return 创建成功的交易响应DTO和201 Created状态码。
     */
    @Operation(summary = "创建一笔新交易", description = "创建一笔新的交易，返回这笔交易信息，请求需要带上访问令牌")
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@Valid @RequestBody CreateTransactionRequest request) {
        TransactionResponse response = transactionService.createTransaction(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * 根据ID获取特定交易详情。
     * @param id 路径变量中的交易ID。
     * @return 交易响应DTO和200 OK状态码。如果未找到，GlobalExceptionHandler会返回404。
     */
    @Operation(summary = "获取特定交易详情", description = "根据ID获取特定交易详情")
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransactionById(@PathVariable String id) {
        TransactionResponse response = transactionService.getTransactionById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取所有交易。
     * @return 所有交易响应DTO的列表和200 OK状态码。
     */
    @Operation(summary = "获取所有交易", description = "获取所有交易")
    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getAllTransactions() {
        List<TransactionResponse> transactions = transactionService.getAllTransactions();
        return ResponseEntity.ok(transactions);
    }

    /**
     * 更新一笔现有交易。
     * @param id 路径变量中的交易ID。
     * @param request 包含更新数据的请求体，通过@Valid进行数据验证。
     * @return 更新后的交易响应DTO和200 OK状态码。如果未找到，GlobalExceptionHandler会返回404。
     */
    @Operation(summary = "更新一笔现有交易", description = "根据ID更新一笔现有交易，返回更新后的交易信息,不存在会报错")
    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(@PathVariable String id, @Valid @RequestBody UpdateTransactionRequest request) {
        TransactionResponse response = transactionService.updateTransaction(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 根据ID删除交易。
     * @param id 路径变量中的交易ID。
     * @return 204 No Content状态码。如果未找到，GlobalExceptionHandler会返回404。
     */
    @Operation(summary = "删除交易",description = "根据ID删除交易,不存在会报错")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable String id) {
        transactionService.deleteTransaction(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content 表示请求成功但没有返回数据。
    }

    /**
     * 分页获取所有交易。
     * @param page 页码 (默认 0)
     * @param size 每页大小 (默认 10)
     * @return 包含交易列表的分页响应对象和 200 OK 状态码
     */
    @Operation(summary = "分页获取所有交易",  description = "分页获取所有交易")
    @GetMapping("/page")
    public ResponseEntity<Page<TransactionResponse>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(transactionService.getAllTransactions(pageable));
    }
}


