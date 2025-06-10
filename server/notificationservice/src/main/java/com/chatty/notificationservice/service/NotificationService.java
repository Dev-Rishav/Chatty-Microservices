package com.chatty.notificationservice.service;




import com.chatty.notificationservice.entity.Notification;

import java.util.List;

public interface NotificationService {
    List<Notification> getNotificationsForUser(String userEmail);
}

