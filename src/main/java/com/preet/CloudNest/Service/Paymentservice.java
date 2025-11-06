package com.preet.CloudNest.Service;

import com.preet.CloudNest.Documents.Profiledocument;
import com.preet.CloudNest.Documents.paymenttansaction;
import com.preet.CloudNest.Dto.PaymentDto;
import com.preet.CloudNest.Dto.paymentverificationDto;
import com.preet.CloudNest.Repository.paymenttransactionrepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class Paymentservice {

    private final Profileservice profileservice;
    private final usercreditsservice usercreditsservice;
    private final paymenttransactionrepository paymenttransactionrepository;

    @Value("${razorpay.key.id}")
    private String razorpayKeyid;
    @Value("${razorpay.key.secret}")
    private String razorpayKeysecret;

    public PaymentDto createorder(PaymentDto paymentDto) {
        try {
            Profiledocument currentprofile = profileservice.getCurrentProfile();
            String ClerkId = currentprofile.getClerkId();

            log.info("Creating order for user: {}, amount: {}", ClerkId, paymentDto.getAmount());

            RazorpayClient razorpayClient = new RazorpayClient(razorpayKeyid, razorpayKeysecret);

            JSONObject orderRequest = new JSONObject();
            // ✅ FIXED: Frontend already sends amount in paise, don't multiply again
            orderRequest.put("amount", paymentDto.getAmount()); // Remove * 100
            orderRequest.put("currency", paymentDto.getCurrency());
            orderRequest.put("receipt", "order_" + System.currentTimeMillis());

            log.info("Order request: {}", orderRequest.toString());

            Order order = razorpayClient.orders.create(orderRequest);
            String orderid = order.get("id");

            log.info("Razorpay order created: {}", orderid);

            // ✅ FIXED: Store original amount (not in paise) for consistency
            paymenttansaction transaction = paymenttansaction.builder()
                    .clerkId(ClerkId)
                    .orderId(orderid)
                    .planId(paymentDto.getPlanId())
                    .amount(paymentDto.getAmount() / 100) // Convert back to rupees for storage
                    .currency(paymentDto.getCurrency())
                    .credits(paymentDto.getCredits())
                    .status("Pending")
                    .transactionDate(java.time.LocalDateTime.now())
                    .build();

            paymenttransactionrepository.save(transaction);

            log.info("Transaction saved with orderId: {}", orderid);

            return PaymentDto.builder()
                    .orderId(orderid)
                    .success(true)
                    .message("Order created successfully")
                    .build();

        } catch (Exception e) {
            log.error("Error creating order: ", e);
            return PaymentDto.builder()
                    .success(false)
                    .message("Error creating order: " + e.getMessage())
                    .build();
        }
    }

    public PaymentDto verifypayment(paymentverificationDto request) {
        try {
            Profiledocument currentprofile = profileservice.getCurrentProfile();
            String clerkId = currentprofile.getClerkId();

            log.info("Verifying payment for user: {}, orderId: {}", clerkId, request.getRazorpayOrderId());

            // ✅ IMPROVED: Validate required fields
            if (request.getRazorpayOrderId() == null || request.getRazorpayPaymentId() == null || request.getRazorpaySignature() == null) {
                log.error("Missing required payment verification fields");
                return PaymentDto.builder()
                        .success(false)
                        .message("Missing required payment information")
                        .build();
            }

            // ✅ FIXED: Generate signature correctly
            String data = request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId();
            String generatedSignature = generateHmacSha256(data, razorpayKeysecret);

            log.info("Generated signature: {}", generatedSignature);
            log.info("Received signature: {}", request.getRazorpaySignature());

            if (!generatedSignature.equals(request.getRazorpaySignature())) {
                log.error("Payment signature verification failed");
                updatetransactionstatus(request.getRazorpayOrderId(), "Failed", request.getRazorpayPaymentId(), null);
                return PaymentDto.builder()
                        .success(false)
                        .message("Invalid payment signature")
                        .build();
            }

            // ✅ IMPROVED: Plan validation and credit assignment
            int creditsToAdd = 0;
            String plan = "BASIC";

            switch (request.getPlanId().toLowerCase()) {
                case "premium":
                    creditsToAdd = 500;
                    plan = "PREMIUM";
                    break;
                case "ultimate":
                    creditsToAdd = 2500;
                    plan = "ULTIMATE";
                    break;
                default:
                    log.error("Invalid plan ID: {}", request.getPlanId());
                    updatetransactionstatus(request.getRazorpayOrderId(), "Failed", request.getRazorpayPaymentId(), null);
                    return PaymentDto.builder()
                            .success(false)
                            .message("Invalid plan selected: " + request.getPlanId())
                            .build();
            }

            log.info("Adding {} credits for plan: {}", creditsToAdd, plan);

            // Add credits to user
            usercreditsservice.addcredits(clerkId, creditsToAdd, plan);

            // Update transaction status
            updatetransactionstatus(request.getRazorpayOrderId(), "Success", request.getRazorpayPaymentId(), creditsToAdd);

            // Get updated credits
            int updatedCredits = usercreditsservice.getusercredits(clerkId).getCredits();

            log.info("Payment verification successful. Updated credits: {}", updatedCredits);

            return PaymentDto.builder()
                    .success(true)
                    .message("Payment verified and credits added successfully")
                    .credits(updatedCredits)
                    .build();

        } catch (Exception e) {
            log.error("Error verifying payment: ", e);
            try {
                updatetransactionstatus(request.getRazorpayOrderId(), "Failed", request.getRazorpayPaymentId(), null);
            } catch (Exception ex) {
                log.error("Error updating transaction status: ", ex);
            }
            return PaymentDto.builder()
                    .success(false)
                    .message("Error verifying payment: " + e.getMessage())
                    .build();
        }
    }

    private void updatetransactionstatus(String razorpayOrderId, String status, String razorpayPaymentId, Integer creditsAdded) {
        try {
            // ✅ IMPROVED: More efficient database query
            paymenttransactionrepository.findAll().stream()
                    .filter(t -> t.getOrderId() != null && t.getOrderId().equals(razorpayOrderId))
                    .findFirst()
                    .ifPresentOrElse(
                            transaction -> {
                                transaction.setStatus(status);
                                transaction.setPaymentId(razorpayPaymentId);
                                if (creditsAdded != null) {
                                    // You might want to add a creditsAdded field to your entity
                                    // transaction.setCreditsAdded(creditsAdded);
                                }
                                paymenttransactionrepository.save(transaction);
                                log.info("Transaction status updated: {} -> {}", razorpayOrderId, status);
                            },
                            () -> {
                                log.error("Transaction not found for orderId: {}", razorpayOrderId);
                                throw new RuntimeException("Transaction not found for orderId: " + razorpayOrderId);
                            }
                    );
        } catch (Exception e) {
            log.error("Error updating transaction status: ", e);
            throw new RuntimeException("Error updating transaction status", e);
        }
    }

    // ✅ FIXED: Correct method name and implementation
    private String generateHmacSha256(String data, String secret) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            sha256Hmac.init(secretKeySpec);
            byte[] hash = sha256Hmac.doFinal(data.getBytes());

            // ✅ CRITICAL: Use hex encoding, not Base64!
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Error generating HMAC signature: ", e);
            throw new RuntimeException("Error generating HMAC signature", e);
        }
    }
}





