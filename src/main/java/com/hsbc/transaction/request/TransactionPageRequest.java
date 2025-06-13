package com.hsbc.transaction.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TransactionPageRequest {

    private LocalDateTime transactionTimeStart;
    private LocalDateTime transactionTimeEnd;

    int pageSize;
    int pageNumber;
}
