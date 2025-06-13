package com.hsbc.transaction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hsbc.transaction.enums.TransactionType;
import com.hsbc.transaction.repository.InMemoryTransactionRepository;
import com.hsbc.transaction.request.CreateTransactionRequest;
import com.hsbc.transaction.request.UpdateTransactionRequest;
import com.hsbc.transaction.response.TransactionResponse;
import com.hsbc.transaction.service.TransactionService;
import com.hsbc.transaction.util.TokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
* TransactionController的集成测试类。
* 使用@SpringBootTest加载完整的Spring应用上下文。
* 使用@AutoConfigureMockMvc自动配置MockMvc，用于模拟HTTP请求。
*/
@SpringBootTest
@AutoConfigureMockMvc
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc; // 用于模拟HTTP请求

    @Autowired
    private ObjectMapper objectMapper; // 用于Java对象和JSON字符串之间的转换

    @Autowired
    private InMemoryTransactionRepository inMemoryTransactionRepository; // 直接注入内存仓库，用于测试前清空数据或预置数据

    @MockBean
    private TransactionService transactionService; // 模拟TransactionService

    /**
     * 在每个测试方法执行前清空内存中的数据，确保测试的独立性。
     */
    @BeforeEach
    void setUp() {
        // 清空内存仓库，确保每个测试都是独立的
        try {
            // 通过反射获取并清空ConcurrentHashMap
            Field field = InMemoryTransactionRepository.class.getDeclaredField("transactions");
            field.setAccessible(true);
            ((ConcurrentHashMap<?, ?>) field.get(inMemoryTransactionRepository)).clear();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            // 在实际项目中，这里会使用更健壮的错误处理或直接在Repository中暴露一个clear方法
        }

        // 清空TokenUtil的tokenStore
        TokenUtil.tokenStore.clear();
    }

    /**
     * 测试成功生成令牌的API端点。
     * @throws Exception 如果MockMvc执行过程中发生错误
     */
    @Test
    void obtainToken_Success() throws Exception {
        mockMvc.perform(post("/api/transactions/token"))
                .andExpect(status().isOk()) // 期望HTTP状态码为200 OK
                .andExpect(jsonPath("$").isString()); // 期望返回一个字符串类型的令牌
    }

    /**
     * 测试成功创建交易的API端点。
     * @throws Exception 如果MockMvc执行过程中发生错误
     */
    @Test
    void createTransaction_Success() throws Exception {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAccountId("account-1");
        request.setAmount(new BigDecimal("200.00"));
        request.setType(TransactionType.DEPOSIT);
        request.setDescription("Initial Deposit");
        request.setPreventDuplicateToken(TokenUtil.generateToken());

        TransactionResponse response = TransactionResponse.builder()
                .id("tx-1")
                .accountId("account-1")
                .amount(new BigDecimal("200.00"))
                .type(TransactionType.DEPOSIT)
                .date(LocalDateTime.now())
                .description("Initial Deposit")
                .build();

        when(transactionService.createTransaction((CreateTransactionRequest) any(CreateTransactionRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("tx-1")))
                .andExpect(jsonPath("$.amount", is(200.00)))
                .andExpect(jsonPath("$.type", is("DEPOSIT")))
                .andExpect(jsonPath("$.description", is("Initial Deposit")));
    }

    /**
     * 测试参数验证失败的创建交易请求。
     * @throws Exception 如果MockMvc执行过程中发生错误
     */
    @Test
    void createTransaction_InvalidRequest() throws Exception {
        CreateTransactionRequest request = new CreateTransactionRequest();
        // 不设置必要字段以触发验证错误
        request.setAmount(new BigDecimal("-100.00")); // 无效金额

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /**
     * 测试成功获取单个交易的API端点。
     * @throws Exception 如果MockMvc执行过程中发生错误
     */
    @Test
    void getTransactionById_Success() throws Exception {
        String id = "some-id";
        TransactionResponse response = TransactionResponse.builder()
                .id(id)
                .accountId("account-1")
                .amount(new BigDecimal("300"))
                .type(TransactionType.DEPOSIT)
                .date(LocalDateTime.now())
                .description("Online Purchase")
                .build();

        when(transactionService.getTransactionById(id)).thenReturn(response);

        mockMvc.perform(get("/api/transactions/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id)))
                .andExpect(jsonPath("$.amount", is(300)));
    }

    /**
     * 测试获取不存在的交易的API端点。
     * @throws Exception 如果MockMvc执行过程中发生错误
     */
    @Test
    void getTransactionById_NotFound() throws Exception {
        String id = "non-existent-id";
        when(transactionService.getTransactionById(id)).thenThrow(new RuntimeException("Transaction not found"));

        mockMvc.perform(get("/api/transactions/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    /**
     * 测试获取所有交易的API端点（无数据）。
     * @throws Exception 如果MockMvc执行过程中发生错误
     */
    @Test
    void getAllTransactions_EmptyList() throws Exception {
        when(transactionService.getAllTransactions()).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    /**
     * 测试获取所有交易的API端点（有数据）。
     * @throws Exception 如果MockMvc执行过程中发生错误
     */
    @Test
    void getAllTransactions_WithData() throws Exception {
        List<TransactionResponse> responses = new ArrayList<>();
        responses.add(TransactionResponse.builder()
                .id("tx-1")
                .accountId("account-1")
                .amount(new BigDecimal("100"))
                .type(TransactionType.DEPOSIT)
                .date(LocalDateTime.now())
                .description("Deposit 1")
                .build());
        responses.add(TransactionResponse.builder()
                .id("tx-2")
                .accountId("account-1")
                .amount(new BigDecimal("200"))
                .type(TransactionType.WITHDRAWAL)
                .date(LocalDateTime.now())
                .description("Withdrawal 1")
                .build());

        when(transactionService.getAllTransactions()).thenReturn(responses);

        mockMvc.perform(get("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].amount", is(100)))
                .andExpect(jsonPath("$[1].amount", is(200)));
    }

    /**
     * 测试成功更新交易的API端点。
     * @throws Exception 如果MockMvc执行过程中发生错误
     */
    @Test
    void updateTransaction_Success() throws Exception {
        String id = "some-id";
        UpdateTransactionRequest request = new UpdateTransactionRequest();
        // 假设UpdateTransactionRequest有setter方法
        // request.setSomeField("newValue");

        TransactionResponse response = TransactionResponse.builder()
                .id(id)
                .accountId("account-1")
                .amount(new BigDecimal("400"))
                .type(TransactionType.DEPOSIT
                )
                .date(LocalDateTime.now())
                .description("Updated Description")
                .build();

        when(transactionService.updateTransaction(eq(id), (UpdateTransactionRequest) any(UpdateTransactionRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/transactions/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id)))
                .andExpect(jsonPath("$.amount", is(400)))
                .andExpect(jsonPath("$.type", is("TRANSFER")));
    }

    /**
     * 测试更新不存在的交易的API端点。
     * @throws Exception 如果MockMvc执行过程中发生错误
     */
    @Test
    void updateTransaction_NotFound() throws Exception {
        String id = "non-existent-id";
        UpdateTransactionRequest request = new UpdateTransactionRequest();
        // 假设UpdateTransactionRequest有setter方法
        // request.setSomeField("newValue");

        when(transactionService.updateTransaction(eq(id), (UpdateTransactionRequest) any(UpdateTransactionRequest.class)))
                .thenThrow(new RuntimeException("Transaction not found"));

        mockMvc.perform(put("/api/transactions/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    /**
     * 测试成功删除交易的API端点。
     * @throws Exception 如果MockMvc执行过程中发生错误
     */
    @Test
    void deleteTransaction_Success() throws Exception {
        String id = "some-id";
        doNothing().when(transactionService).deleteTransaction(id);

        mockMvc.perform(delete("/api/transactions/{id}", id))
                .andExpect(status().isNoContent());
    }

    /**
     * 测试删除不存在的交易的API端点。
     * @throws Exception 如果MockMvc执行过程中发生错误
     */
    @Test
    void deleteTransaction_NotFound() throws Exception {
        String id = "non-existent-id";
        doThrow(new RuntimeException("Transaction not found")).when(transactionService).deleteTransaction(id);

        mockMvc.perform(delete("/api/transactions/{id}", id))
                .andExpect(status().isNotFound());
    }

    /**
     * 测试分页获取所有交易的API端点（默认参数）。
     * @throws Exception 如果MockMvc执行过程中发生错误
     */
    @Test
    void getAllTransactions_Pageable_DefaultParams() throws Exception {
        List<TransactionResponse> responses = new ArrayList<>();
        responses.add(TransactionResponse.builder()
                .id("tx-1")
                .accountId("account-1")
                .amount(new BigDecimal("100"))
                .type(TransactionType.DEPOSIT)
                .date(LocalDateTime.now())
                .description("Deposit 1")
                .build());

        Page<TransactionResponse> page = new PageImpl<>(responses);
        when(transactionService.getAllTransactions((Pageable) any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/transactions/page")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].amount", is(100)));
    }

    /**
     * 测试分页获取所有交易的API端点（自定义参数）。
     * @throws Exception 如果MockMvc执行过程中发生错误
     */
    @Test
    void getAllTransactions_Pageable_CustomParams() throws Exception {
        List<TransactionResponse> responses = new ArrayList<>();
        responses.add(TransactionResponse.builder()
                .id("tx-2")
                .accountId("account-1")
                .amount(new BigDecimal("200"))
                .type(TransactionType.WITHDRAWAL)
                .date(LocalDateTime.now())
                .description("Withdrawal 1")
                .build());

        Page<TransactionResponse> page = new PageImpl<>(responses);
        when(transactionService.getAllTransactions((Pageable) any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/transactions/page")
                        .param("page", "1")
                        .param("size", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].amount", is(200)));
    }
}