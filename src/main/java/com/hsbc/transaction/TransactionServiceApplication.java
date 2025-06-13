package com.hsbc.transaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Spring Boot应用程序的入口点。
 * 它们自动配置Spring应用并启用组件扫描。
 */
@SpringBootApplication
@EnableCaching // 启用 Spring 缓存功能
public class TransactionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransactionServiceApplication.class, args);
    }

}
