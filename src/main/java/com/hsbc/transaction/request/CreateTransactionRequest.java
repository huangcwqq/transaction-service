package com.hsbc.transaction.request;

import com.hsbc.transaction.enums.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateTransactionRequest {

    // 这里的 token 由后端生成传输给前端，前端创建交易请求时传输该ID，可以更好地做幂等性处理，防止重复请求。
    @NotBlank(message = "防重token不能为空")
    private String preventDuplicateToken;

    // 账号 ID
    @NotBlank(message = "账号ID不能为空")
    private String accountId;

    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "金额必须大于0")
    private BigDecimal amount; // 交易金额

    @NotNull(message = "交易类型不能为空")
    private TransactionType type; // 交易类型 (DEPOSIT/WITHDRAWAL)

    @NotBlank(message = "描述不能为空")
    private String description; // 交易描述
}
