package com.hsbc.transaction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hsbc.transaction.enums.TransactionType;
import com.hsbc.transaction.model.Transaction;
import com.hsbc.transaction.repository.InMemoryTransactionRepository;
import com.hsbc.transaction.request.CreateTransactionRequest;
import com.hsbc.transaction.request.UpdateTransactionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
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
public class TransactionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc; // 用于模拟HTTP请求

    @Autowired
    private ObjectMapper objectMapper; // 用于Java对象和JSON字符串之间的转换

    @Autowired
    private InMemoryTransactionRepository inMemoryTransactionRepository; // 直接注入内存仓库，用于测试前清空数据或预置数据

    /**
     * 在每个测试方法执行前清空内存中的数据，确保测试的独立性。
     */
    @BeforeEach
    void setUp() {
        // 清空内存仓库，确保每个测试都是独立的
        // 尽管 InMemoryTransactionRepository 没有直接的 clear 方法，我们可以通过反射或添加一个测试专用的方法来做
        // 或者，更简单的方法是知道它是一个 ConcurrentHashMap，可以获取其内部map并清空
        try {
            // 通过反射获取并清空ConcurrentHashMap
            Field field = InMemoryTransactionRepository.class.getDeclaredField("transactions");
            field.setAccessible(true);
            ((ConcurrentHashMap<?, ?>) field.get(inMemoryTransactionRepository)).clear();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            // 在实际项目中，这里会使用更健壮的错误处理或直接在Repository中暴露一个clear方法
        }
    }

    /**
     * 测试成功创建交易的API端点。
     * @throws Exception 如果MockMvc执行过程中发生错误
     */
    @Test
    void createTransaction_Success() throws Exception {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setId("transaction-1");
        request.setAccountId("account-1");
        request.setAmount(new BigDecimal("200.00"));
        request.setType(TransactionType.DEPOSIT);
        request.setDescription("Initial Deposit");

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON) // 设置请求头为JSON类型
                        .content(objectMapper.writeValueAsString(request))) // 将请求对象转换为JSON字符串作为请求体
                .andExpect(status().isCreated()) // 期望HTTP状态码为201 Created
                .andExpect(jsonPath("$.id").exists()) // 期望响应JSON中包含id字段
                .andExpect(jsonPath("$.amount", is(200.00))) // 期望金额字段值正确
                .andExpect(jsonPath("$.type", is("DEPOSIT"))) // 期望类型字段值正确
                .andExpect(jsonPath("$.description", is("Initial Deposit"))); // 期望描述字段值正确

        // 验证内存仓库中是否确实添加了交易
        assertEquals(1, inMemoryTransactionRepository.findAll().size());
    }

    /**
     * 测试创建交易时，请求体参数验证失败的API端点。
     * @throws Exception 如果MockMvc执行过程中发生错误
     */
    @Test
    void createTransaction_InvalidInput_ReturnsBadRequest() throws Exception {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setId("transaction-1");
        request.setAccountId("account-1");
        request.setAmount(new BigDecimal("-10.00")); // 无效金额
        request.setType(null); // 无效类型
        request.setDescription(""); // 无效描述

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()) // 期望HTTP状态码为400 Bad Request
                .andExpect(jsonPath("$.status", is(400))) // 期望响应JSON中的status字段为400
                .andExpect(jsonPath("$.error", is("Bad Request"))) // 期望error字段为"Bad Request"
                .andExpect(jsonPath("$.message", containsString("金额必须大于0"))) // 期望message中包含金额验证错误
                .andExpect(jsonPath("$.message", containsString("交易类型不能为空"))) // 期望message中包含类型验证错误
                .andExpect(jsonPath("$.message", containsString("描述不能为空"))); // 期望message中包含描述验证错误

        // 验证内存仓库中没有添加交易
        assertEquals(0, inMemoryTransactionRepository.findAll().size());
    }

    /**
     * 测试成功获取所有交易的API端点。
     * @throws Exception 如果MockMvc执行过程中发生错误
     */
    @Test
    void getAllTransactions_Success() throws Exception {
        // 预置一些数据到内存仓库
        inMemoryTransactionRepository.save(new Transaction("id1", "account-1",new BigDecimal("100"), TransactionType.DEPOSIT, LocalDateTime.now(), "Deposit 1"));
        inMemoryTransactionRepository.save(new Transaction("id2", "account-2",new BigDecimal("50"), TransactionType.WITHDRAWAL, LocalDateTime.now(), "Withdrawal 1"));

        mockMvc.perform(get("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // 期望HTTP状态码为200 OK
                .andExpect(jsonPath("$", hasSize(2))) // 期望返回两个交易
                .andExpect(jsonPath("$[0].id", anyOf(is("id1"), is("id2")))) // 期望包含id1或id2
                .andExpect(jsonPath("$[1].id", anyOf(is("id1"), is("id2"))));
    }

    /**
     * 测试成功获取单个交易的API端点。
     * @throws Exception 如果MockMvc执行过程中发生错误
     */
    @Test
    void getTransactionById_Success() throws Exception {
        String id = "some-id";
        // 预置数据
        inMemoryTransactionRepository.save(new Transaction(id, "account-1",new BigDecimal("300"), TransactionType.DEPOSIT, LocalDateTime.now(), "Online Purchase"));

        mockMvc.perform(get("/api/transactions/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // 期望HTTP状态码为200 OK
                .andExpect(jsonPath("$.id", is(id))) // 期望ID匹配
                .andExpect(jsonPath("$.amount", is(300)));
    }

    /**
     * 测试获取不存在的交易时API端点返回404。
     * @throws Exception 如果MockMvc执行过程中发生错误
     */
    @Test
    void getTransactionById_NotFound_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/transactions/{id}", "non-existent-id")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()) // 期望HTTP状态码为404 Not Found
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", containsString("交易未找到")));
    }

    /**
     * 测试成功更新交易的API端点。
     * @throws Exception 如果MockMvc执行过程中发生错误
     */
    @Test
    void updateTransaction_Success() throws Exception {
        String id = "update-id";
        // 预置初始数据
        inMemoryTransactionRepository.save(new Transaction(id,"account-1", new BigDecimal("100"), TransactionType.DEPOSIT, LocalDateTime.now(), "Original Desc"));

        UpdateTransactionRequest updatedRequest = new UpdateTransactionRequest();
        updatedRequest.setAmount(new BigDecimal("120.00"));
        updatedRequest.setType(TransactionType.WITHDRAWAL);
        updatedRequest.setDescription("Updated Desc");

        mockMvc.perform(put("/api/transactions/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedRequest)))
                .andExpect(status().isOk()) // 期望HTTP状态码为200 OK
                .andExpect(jsonPath("$.id", is(id)))
                .andExpect(jsonPath("$.amount", is(120.00)))
                .andExpect(jsonPath("$.type", is("WITHDRAWAL")))
                .andExpect(jsonPath("$.description", is("Updated Desc")));

        // 验证内存仓库中的数据是否已更新
        assertTrue(inMemoryTransactionRepository.findById(id).isPresent());
        assertEquals(new BigDecimal("120.00"), inMemoryTransactionRepository.findById(id).get().getAmount());    }

    /**
     * 测试更新不存在的交易时API端点返回404。
     * @throws Exception 如果MockMvc执行过程中发生错误
     */
    @Test
    void updateTransaction_NotFound_ReturnsNotFound() throws Exception {
        UpdateTransactionRequest request = new UpdateTransactionRequest();
        request.setAmount(new BigDecimal("100.00"));
        request.setType(TransactionType.DEPOSIT);
        request.setDescription("Test Desc");

        mockMvc.perform(put("/api/transactions/{id}", "non-existent-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound()) // 期望HTTP状态码为404 Not Found
                .andExpect(jsonPath("$.message", containsString("无法更新，交易未找到")));
    }

    /**
     * 测试更新交易时，请求体参数验证失败的API端点。
     * @throws Exception 如果MockMvc执行过程中发生错误
     */
    @Test
    void updateTransaction_InvalidInput_ReturnsBadRequest() throws Exception {
        String id = "valid-id";
        inMemoryTransactionRepository.save(new Transaction(id, "account-1",new BigDecimal("100"), TransactionType.DEPOSIT, LocalDateTime.now(), "Original Desc"));

        UpdateTransactionRequest request = new UpdateTransactionRequest();
        request.setAmount(new BigDecimal("-10.00")); // 无效金额
        request.setType(null); // 无效类型

        mockMvc.perform(put("/api/transactions/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()) // 期望HTTP状态码为400 Bad Request
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", containsString("金额必须大于0")))
                .andExpect(jsonPath("$.message", containsString("交易类型不能为空")));
    }

    /**
     * 测试成功删除交易的API端点。
     * @throws Exception 如果MockMvc执行过程中发生错误
     */
    @Test
    void deleteTransaction_Success() throws Exception {
        String id = "delete-id";
        // 预置数据
        inMemoryTransactionRepository.save(new Transaction(id, "account-1",new BigDecimal("100"), TransactionType.DEPOSIT, LocalDateTime.now(), "To be deleted"));

        mockMvc.perform(delete("/api/transactions/{id}", id))
                .andExpect(status().isNoContent()); // 期望HTTP状态码为204 No Content

        // 验证内存仓库中数据是否已删除
        assertFalse(inMemoryTransactionRepository.existsById(id));
    }

    /**
     * 测试删除不存在的交易时API端点返回404。
     * @throws Exception 如果MockMvc执行过程中发生错误
     */
    @Test
    void deleteTransaction_NotFound_ReturnsNotFound() throws Exception {
        mockMvc.perform(delete("/api/transactions/{id}", "non-existent-id"))
                .andExpect(status().isNotFound()) // 期望HTTP状态码为404 Not Found
                .andExpect(jsonPath("$.message", containsString("无法删除，交易未找到")));
    }

    @Test
    void getAllTransactionsPage_Success() throws Exception {
        // 预置一些数据到内存仓库
        inMemoryTransactionRepository.save(new Transaction("id1", "account-1",new BigDecimal("100"), TransactionType.DEPOSIT, LocalDateTime.now(), "Deposit 1"));
        inMemoryTransactionRepository.save(new Transaction("id2", "account-2",new BigDecimal("50"), TransactionType.WITHDRAWAL, LocalDateTime.now(), "Withdrawal 1"));

        mockMvc.perform(get("/api/transactions/page")
                        .param("page", "0")
                        .param("size", "2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id", is("id2")))
                .andExpect(jsonPath("$.content[1].id", is("id1")))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.size", is(2)))
                .andExpect(jsonPath("$.number", is(0)));
    }
}