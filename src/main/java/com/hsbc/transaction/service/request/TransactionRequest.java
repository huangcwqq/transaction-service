package com.hsbc.transaction.service.request;

import com.hsbc.transaction.service.enums.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
        * 用于创建或更新交易的请求数据传输对象。
        * 包含JSR 303 (Jakarta Validation) 注解进行数据验证。
        */
@Data
public class TransactionRequest {
    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "金额必须大于0")
    private BigDecimal amount; // 交易金额

    @NotNull(message = "交易类型不能为空")
    private TransactionType type; // 交易类型 (DEPOSIT/WITHDRAWAL)

    @NotBlank(message = "描述不能为空")
    private String description; // 交易描述
}
