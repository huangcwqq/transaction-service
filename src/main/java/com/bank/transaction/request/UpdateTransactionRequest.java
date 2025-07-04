package com.bank.transaction.request;

import com.bank.transaction.enums.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTransactionRequest {
    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "金额必须大于0")
    private BigDecimal amount; // 交易金额

    @NotNull(message = "交易类型不能为空")
    private TransactionType type; // 交易类型 (DEPOSIT/WITHDRAWAL)

    @NotBlank(message = "描述不能为空")
    private String description; // 交易描述
}
