package com.preet.CloudNest.Controller;

import com.preet.CloudNest.Dto.PaymentDto;
import com.preet.CloudNest.Dto.paymentverificationDto;
import com.preet.CloudNest.Service.Paymentservice;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class Paymentcontroller {

    private final Paymentservice paymentsService;

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody PaymentDto paymentDto) {
        PaymentDto response = paymentsService.createorder(paymentDto);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/verify-payment")
    public ResponseEntity<?> verifypayment(@RequestBody paymentverificationDto request) {
        try {
            System.out.println("Incoming verify request: " + request);

            PaymentDto response = paymentsService.verifypayment(request);

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(400).body(response);
            }
        } catch (Exception e) {
            e.printStackTrace(); // ✅ check logs for real cause
            return ResponseEntity.status(500).body("Verification failed: " + e.getMessage());
        }
    }
}
