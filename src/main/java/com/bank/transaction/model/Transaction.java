package com.bank.transaction.model;

import com.bank.transaction.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Transaction {

    private String id;              // 交易唯一标识符，使用UUID生成
    private String accountId;       // 相关的账户 ID，简化为单一账户系统
    private BigDecimal amount;      // 交易金额，使用BigDecimal确保精度
    private TransactionType type;   // 交易类型，例如存款或取款
    private LocalDateTime date;     // 交易发生日期和时间
    private String description;     // 交易描述
}
