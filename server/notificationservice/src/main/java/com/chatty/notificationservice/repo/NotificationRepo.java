package com.chatty.notificationservice.repo;



import com.chatty.notificationservice.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepo extends JpaRepository<Notification,Long> {
    List<Notification> findByReceiverOrderByCreatedAtDesc(String email);
}

