package com.hsbc.transaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot应用程序的入口点。
 * @SpringBootApplication 包含了@Configuration, @EnableAutoConfiguration, @ComponentScan，
 * 它们自动配置Spring应用并启用组件扫描。
 */
@SpringBootApplication
public class TransactionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransactionServiceApplication.class, args);
    }

}
