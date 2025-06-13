package com.hsbc.transaction.response;

import com.hsbc.transaction.enums.TransactionType;
import com.hsbc.transaction.model.Transaction;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用于返回交易信息的响应数据传输对象。
 * 使用@Builder注解方便构建对象。
 */
@Data
@Builder
public class TransactionResponse {
    private String id;                // 交易唯一标识符
    private BigDecimal amount;        // 交易金额
    private TransactionType type;     // 交易类型
    private LocalDateTime date;       // 交易发生日期和时间
    private String description;       // 交易描述

    /**
     * 将Transaction模型转换为TransactionResponse DTO。
     * @param transaction 交易模型
     * @return 交易响应DTO
     */
    public static TransactionResponse fromEntity(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .date(transaction.getDate())
                .description(transaction.getDescription())
                .build();
    }
}
