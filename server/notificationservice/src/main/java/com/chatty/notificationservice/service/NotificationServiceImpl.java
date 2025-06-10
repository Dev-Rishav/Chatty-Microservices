package com.chatty.notificationservice.service;



import com.chatty.notificationservice.entity.Notification;
import com.chatty.notificationservice.repo.NotificationRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepo notificationRepository;

    @Override
    public List<Notification> getNotificationsForUser(String userEmail) {
        return notificationRepository.findByReceiverOrderByCreatedAtDesc(userEmail);
    }

}

