package com.preet.CloudNest.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor

@NoArgsConstructor
public class paymentverificationDto {

    @JsonProperty("razorpay_order_id")
    private String razorpayOrderId;
    @JsonProperty("razorpay_payment_id")
    private String razorpayPaymentId;
    @JsonProperty("razorpay_signature")
    private String razorpaySignature;
    private String planId;

}
