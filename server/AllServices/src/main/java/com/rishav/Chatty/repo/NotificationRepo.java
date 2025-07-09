package com.rishav.Chatty.repo;

import com.rishav.Chatty.entities.Notification;
import com.rishav.Chatty.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepo extends JpaRepository<Notification,Long> {
    List<Notification> findByReceiverEmailOrderByCreatedAtDesc(String email);
    Optional<Notification> findByReceiverAndMessageContaining(Users receiver, String messageContent);
}
