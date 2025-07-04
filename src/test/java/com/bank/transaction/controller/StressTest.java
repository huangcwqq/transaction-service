package com.bank.transaction.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

@SpringBootTest
@AutoConfigureMockMvc
public class StressTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void stressTestCreateTransaction() throws Exception {
        int threadCount = 20; // 并发线程数
        int requestPerThread = 10; // 每线程请求数
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        CountDownLatch latch = new CountDownLatch(threadCount * requestPerThread);
        long start = System.currentTimeMillis();
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                for (int j = 0; j < requestPerThread; j++) {
                    try {
                        // 获取token
                        String token = mockMvc.perform(post("/api/transactions/token"))
                                .andReturn().getResponse().getContentAsString();
                        String json = String.format(
                                "{\"preventDuplicateToken\":\"%s\",\"accountId\":\"acc-%d\",\"amount\":100.00,\"type\":\"DEPOSIT\",\"description\":\"desc\"}",
                                token, Thread.currentThread().threadId()
                        );
                        mockMvc.perform(post("/api/transactions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                                .andReturn();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }
        latch.await();
        executor.shutdown();
        long end = System.currentTimeMillis();
        System.out.printf("压力测试完成，总耗时：%d ms，每秒QPS：%.2f\n", (end - start), (threadCount * requestPerThread * 1000.0 / (end - start)));
    }

    @Test
    void stressTestGetTransactionById() throws Exception {
        int total = 100;
        String[] ids = new String[total];
        for (int i = 0; i < total; i++) {
            String token = mockMvc.perform(get("/api/transactions/token")).andReturn().getResponse().getContentAsString();
            String json = String.format(
                    "{\"preventDuplicateToken\":\"%s\",\"accountId\":\"acc-%d\",\"amount\":100.00,\"type\":\"DEPOSIT\",\"description\":\"desc\"}",
                    token, i
            );
            String id = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                    .readTree(mockMvc.perform(post("/api/transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                            .andReturn().getResponse().getContentAsString())
                    .get("id").asText();
            ids[i] = id;
        }
        int threadCount = 20;
        int requestPerThread = 10;
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        CountDownLatch latch = new CountDownLatch(threadCount * requestPerThread);
        long start = System.currentTimeMillis();
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                for (int j = 0; j < requestPerThread; j++) {
                    try {
                        String id = ids[(int) (Math.random() * total)];
                        mockMvc.perform(get("/api/transactions/" + id))
                                .andReturn();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }
        latch.await();
        executor.shutdown();
        long end = System.currentTimeMillis();
        System.out.printf("GetTransactionById压力测试完成，总耗时：%d ms，每秒QPS：%.2f\n", (end - start), (threadCount * requestPerThread * 1000.0 / (end - start)));
    }

    @Test
    void stressTestGetAllTransactions() throws Exception {
        int threadCount = 10;
        int requestPerThread = 10;
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        CountDownLatch latch = new CountDownLatch(threadCount * requestPerThread);
        long start = System.currentTimeMillis();
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                for (int j = 0; j < requestPerThread; j++) {
                    try {
                        mockMvc.perform(get("/api/transactions"))
                                .andReturn();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }
        latch.await();
        executor.shutdown();
        long end = System.currentTimeMillis();
        System.out.printf("GetAllTransactions压力测试完成，总耗时：%d ms，每秒QPS：%.2f\n", (end - start), (threadCount * requestPerThread * 1000.0 / (end - start)));
    }

    @Test
    void stressTestUpdateTransaction() throws Exception {
        int total = 50;
        String[] ids = new String[total];
        for (int i = 0; i < total; i++) {
            String token = mockMvc.perform(get("/api/transactions/token")).andReturn().getResponse().getContentAsString();
            String json = String.format(
                    "{\"preventDuplicateToken\":\"%s\",\"accountId\":\"acc-%d\",\"amount\":100.00,\"type\":\"DEPOSIT\",\"description\":\"desc\"}",
                    token, i
            );
            String id = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                    .readTree(mockMvc.perform(post("/api/transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                            .andReturn().getResponse().getContentAsString())
                    .get("id").asText();
            ids[i] = id;
        }
        int threadCount = 10;
        int requestPerThread = 10;
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        CountDownLatch latch = new CountDownLatch(threadCount * requestPerThread);
        long start = System.currentTimeMillis();
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                for (int j = 0; j < requestPerThread; j++) {
                    try {
                        String id = ids[(int) (Math.random() * total)];
                        String updateJson = "{\"amount\":200.00,\"type\":\"WITHDRAWAL\",\"description\":\"update-desc\"}";
                        mockMvc.perform(put("/api/transactions/" + id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson))
                                .andReturn();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }
        latch.await();
        executor.shutdown();
        long end = System.currentTimeMillis();
        System.out.printf("UpdateTransaction压力测试完成，总耗时：%d ms，每秒QPS：%.2f\n", (end - start), (threadCount * requestPerThread * 1000.0 / (end - start)));
    }

    @Test
    void stressTestDeleteTransaction() throws Exception {
        int total = 50;
        String[] ids = new String[total];
        for (int i = 0; i < total; i++) {
            String token = mockMvc.perform(get("/api/transactions/token")).andReturn().getResponse().getContentAsString();
            String json = String.format(
                    "{\"preventDuplicateToken\":\"%s\",\"accountId\":\"acc-%d\",\"amount\":100.00,\"type\":\"DEPOSIT\",\"description\":\"desc\"}",
                    token, i
            );
            String id = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                    .readTree(mockMvc.perform(post("/api/transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                            .andReturn().getResponse().getContentAsString())
                    .get("id").asText();
            ids[i] = id;
        }
        int threadCount = 10;
        int requestPerThread = 10;
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        CountDownLatch latch = new CountDownLatch(threadCount * requestPerThread);
        long start = System.currentTimeMillis();
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                for (int j = 0; j < requestPerThread; j++) {
                    try {
                        String id = ids[(int) (Math.random() * total)];
                        mockMvc.perform(delete("/api/transactions/" + id))
                                .andReturn();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }
        latch.await();
        executor.shutdown();
        long end = System.currentTimeMillis();
        System.out.printf("DeleteTransaction压力测试完成，总耗时：%d ms，每秒QPS：%.2f\n", (end - start), (threadCount * requestPerThread * 1000.0 / (end - start)));
    }

    @Test
    void stressTestGetAllTransactionsPage() throws Exception {
        int threadCount = 10;
        int requestPerThread = 10;
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        CountDownLatch latch = new CountDownLatch(threadCount * requestPerThread);
        long start = System.currentTimeMillis();
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                for (int j = 0; j < requestPerThread; j++) {
                    try {
                        int page = (int) (Math.random() * 5);
                        int size = 5 + (int) (Math.random() * 10);
                        mockMvc.perform(get("/api/transactions/page?page=" + page + "&size=" + size))
                                .andReturn();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }
        latch.await();
        executor.shutdown();
        long end = System.currentTimeMillis();
        System.out.printf("GetAllTransactionsPage压力测试完成，总耗时：%d ms，每秒QPS：%.2f\n", (end - start), (threadCount * requestPerThread * 1000.0 / (end - start)));
    }
}
