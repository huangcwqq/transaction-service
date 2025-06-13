package com.hsbc.transaction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hsbc.transaction.request.CreateTransactionRequest;
import com.hsbc.transaction.request.UpdateTransactionRequest;
import com.hsbc.transaction.util.TokenUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
* TransactionController的集成测试类。
* 使用@SpringBootTest加载完整的Spring应用上下文。
* 使用@AutoConfigureMockMvc自动配置MockMvc，用于模拟HTTP请求。
*/
@SpringBootTest
@AutoConfigureMockMvc
public class TransactionControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testObtainToken_Success() throws Exception {
        mockMvc.perform(get("/api/transactions/token"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(emptyOrNullString())));
    }

    @Test
    void testCreateTransaction_Success() throws Exception {
        CreateTransactionRequest createRequest = new CreateTransactionRequest();
        // 设置所有必填字段
        createRequest.setPreventDuplicateToken(com.hsbc.transaction.util.TokenUtil.generateToken());
        createRequest.setAccountId("test-account-001");
        createRequest.setAmount(new java.math.BigDecimal("100.00"));
        createRequest.setType(com.hsbc.transaction.enums.TransactionType.DEPOSIT);
        createRequest.setDescription("测试存款");
        String json = objectMapper.writeValueAsString(createRequest);
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void testCreateTransaction_InvalidRequest() throws Exception {
        CreateTransactionRequest createRequest = new CreateTransactionRequest();
        // 不设置必填字段，触发@Valid校验失败
        String json = objectMapper.writeValueAsString(createRequest);
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetTransactionById_Success() throws Exception {
        // 先创建一条合法的交易，获取id
        CreateTransactionRequest createRequest = new CreateTransactionRequest();
        createRequest.setPreventDuplicateToken(TokenUtil.generateToken());
        createRequest.setAccountId("test-account-001");
        createRequest.setAmount(new java.math.BigDecimal("100.00"));
        createRequest.setType(com.hsbc.transaction.enums.TransactionType.DEPOSIT);
        createRequest.setDescription("查询单条交易");
        String json = objectMapper.writeValueAsString(createRequest);
        String id = objectMapper.readTree(mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andReturn().getResponse().getContentAsString()).get("id").asText();
        // 查询该交易
        mockMvc.perform(get("/api/transactions/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.accountId").value("test-account-001"))
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.type").value("DEPOSIT"))
                .andExpect(jsonPath("$.description").value("查询单条交易"));
    }

    @Test
    void testGetTransactionById_NotFound() throws Exception {
        mockMvc.perform(get("/api/transactions/not-exist-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllTransactions_Success() throws Exception {
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testUpdateTransaction_Success() throws Exception {
        // 先创建一条合法的交易，获取id
        CreateTransactionRequest createRequest = new CreateTransactionRequest();
        createRequest.setPreventDuplicateToken(TokenUtil.generateToken());
        createRequest.setAccountId("test-account-001");
        createRequest.setAmount(new java.math.BigDecimal("100.00"));
        createRequest.setType(com.hsbc.transaction.enums.TransactionType.DEPOSIT);
        createRequest.setDescription("测试更新前");
        String json = objectMapper.writeValueAsString(createRequest);
        String id = objectMapper.readTree(mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andReturn().getResponse().getContentAsString()).get("id").asText();
        // 构造更新请求
        UpdateTransactionRequest updateRequest = new UpdateTransactionRequest();
        updateRequest.setAmount(new java.math.BigDecimal("200.00"));
        updateRequest.setType(com.hsbc.transaction.enums.TransactionType.WITHDRAWAL);
        updateRequest.setDescription("测试更新后");
        String updateJson = objectMapper.writeValueAsString(updateRequest);
        mockMvc.perform(put("/api/transactions/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.amount").value(200.00))
                .andExpect(jsonPath("$.type").value("WITHDRAWAL"))
                .andExpect(jsonPath("$.description").value("测试更新后"));
    }

    @Test
    void testUpdateTransaction_NotFound() throws Exception {
        // 构造一个合法的更新请求
        UpdateTransactionRequest updateRequest = new UpdateTransactionRequest();
        updateRequest.setAmount(new java.math.BigDecimal("300.00"));
        updateRequest.setType(com.hsbc.transaction.enums.TransactionType.DEPOSIT);
        updateRequest.setDescription("不存在ID的更新");
        String updateJson = objectMapper.writeValueAsString(updateRequest);
        mockMvc.perform(put("/api/transactions/not-exist-id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateTransaction_InvalidRequest() throws Exception {
        // 先创建一条合法的交易，获取id
        CreateTransactionRequest createRequest = new CreateTransactionRequest();
        createRequest.setPreventDuplicateToken(TokenUtil.generateToken());
        createRequest.setAccountId("test-account-001");
        createRequest.setAmount(new java.math.BigDecimal("100.00"));
        createRequest.setType(com.hsbc.transaction.enums.TransactionType.DEPOSIT);
        createRequest.setDescription("测试无效更新");
        String json = objectMapper.writeValueAsString(createRequest);
        String id = objectMapper.readTree(mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andReturn().getResponse().getContentAsString()).get("id").asText();
        // 构造无效的更新请求（不设置必填字段）
        UpdateTransactionRequest updateRequest = new UpdateTransactionRequest();
        String updateJson = objectMapper.writeValueAsString(updateRequest);
        mockMvc.perform(put("/api/transactions/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteTransaction_Success() throws Exception {
        // 先创建一条合法的交易，保证被删除的id存在
        CreateTransactionRequest createRequest = new CreateTransactionRequest();
        createRequest.setPreventDuplicateToken(TokenUtil.generateToken());
        createRequest.setAccountId("test-account-001");
        createRequest.setAmount(new java.math.BigDecimal("100.00"));
        createRequest.setType(com.hsbc.transaction.enums.TransactionType.DEPOSIT);
        createRequest.setDescription("测试删除");
        String json = objectMapper.writeValueAsString(createRequest);
        String id = objectMapper.readTree(mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andReturn().getResponse().getContentAsString()).get("id").asText();
        // 执行删除
        mockMvc.perform(delete("/api/transactions/" + id))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteTransaction_NotFound() throws Exception {
        mockMvc.perform(delete("/api/transactions/not-exist-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllTransactionsPage_Default() throws Exception {
        mockMvc.perform(get("/api/transactions/page"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testGetAllTransactionsPage_Custom() throws Exception {
        mockMvc.perform(get("/api/transactions/page?page=2&size=3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}
