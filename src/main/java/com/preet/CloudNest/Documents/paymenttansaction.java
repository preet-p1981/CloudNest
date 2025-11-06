package com.preet.CloudNest.Documents;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "payment_transactions")
public class paymenttansaction {

    private String id;
    private String clerkId;
    private String orderId;
    private String paymentId;
    private String planId;
    private Integer amount;
    private String currency;
    private int credits;
    private String status;
    private LocalDateTime transactionDate;



}
