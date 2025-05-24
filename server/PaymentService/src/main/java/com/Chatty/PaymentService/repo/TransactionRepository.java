package com.Chatty.PaymentService.repo;

import com.Chatty.PaymentService.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    List<Transaction> findByUserId(String userId);
}
