package com.Chatty.PaymentService.controllers;

import com.Chatty.PaymentService.dto.CreateOrderRequest;
import com.Chatty.PaymentService.entities.Transaction;
import com.Chatty.PaymentService.services.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @GetMapping
    public String greet(){
        return "Hello World";
    }

    private PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(paymentService.createRazorpayOrder(request));
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(HttpServletRequest request, @RequestBody String payload,
                                                @RequestHeader("X-Razorpay-Signature") String signature) {
        return paymentService.handleWebhook(payload, signature);
    }

    @GetMapping("/transactions/{userId}")
    public ResponseEntity<List<Transaction>> getTransactionHistory(@PathVariable String userId) {
        return ResponseEntity.ok(paymentService.getTransactionsByUser(userId));
    }
}
