package com.Chatty.PaymentService.services;


import com.Chatty.PaymentService.dto.CreateOrderRequest;
import com.Chatty.PaymentService.entities.Transaction;
import com.Chatty.PaymentService.repo.TransactionRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import jakarta.annotation.PostConstruct;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Service
public class PaymentService {

    @Value("${razorpay.key_id}")
    private String key;

    @Value("${razorpay.key_secret}")
    private String secret;

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    private RazorpayClient razorpayClient;

    private final TransactionRepository transactionRepository;

    public PaymentService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @PostConstruct
    public void init() throws Exception {
        razorpayClient = new RazorpayClient(key, secret);
    }

    public String createRazorpayOrder(CreateOrderRequest request) {
        try {
            JSONObject options = new JSONObject();
            options.put("amount", request.getAmount());
            options.put("currency", request.getCurrency());
            options.put("receipt", "txn_" + System.currentTimeMillis());
            options.put("payment_capture", 1);
            Order order=razorpayClient.orders.create(options);
            return order.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create order", e);
        }
    }

    public ResponseEntity<String> handleWebhook(String payload, String signature) {
        try {
            String computedSignature = hmacSha256(payload, webhookSecret);
            if (!computedSignature.equals(signature)) {
                return ResponseEntity.status(400).body("Invalid signature");
            }

            JSONObject json = new JSONObject(payload);
            String event = json.getString("event");

            if (event.equals("payment.captured")) {
                JSONObject entity = json.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity");
                String paymentId = entity.getString("id");
                String orderId = entity.getString("order_id");
                int amount = entity.getInt("amount");
                String userId = entity.optString("notes.userId", "unknown");

                Transaction txn = new Transaction();
                txn.setPaymentId(paymentId);
                txn.setOrderId(orderId);
                txn.setAmount(amount);
                txn.setStatus("captured");
                txn.setUserId(userId);
                txn.setCreatedAt(new Date());

                transactionRepository.save(txn);
            }

            return ResponseEntity.ok("Webhook received");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error handling webhook");
        }
    }

    public List<Transaction> getTransactionsByUser(String userId) {
        return transactionRepository.findByUserId(userId);
    }

    private String hmacSha256(String data, String secret) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hash = mac.doFinal(data.getBytes());
        return new String(Base64.getEncoder().encode(hash));
    }
}
