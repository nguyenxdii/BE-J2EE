package com.j2ee.carbooking.dto.response;

import com.j2ee.carbooking.enums.TransactionStatus;
import com.j2ee.carbooking.enums.TransactionType;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TransactionResponse {
    private String id;
    private String userId;
    private String userName;
    private TransactionType type;
    private Double amount;
    private Double balanceBefore;
    private Double balanceAfter;
    private String refType;
    private String refId;
    private String description;
    private TransactionStatus status;
    private LocalDateTime createdAt;
}
