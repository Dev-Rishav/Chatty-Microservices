package com.rishav.Chatty.services;

import com.rishav.Chatty.dto.ContactRequestDTO;
import com.rishav.Chatty.dto.NotificationDTO;
import com.rishav.Chatty.entities.Contact;
import com.rishav.Chatty.entities.ContactRequest;
import com.rishav.Chatty.entities.Notification;
import com.rishav.Chatty.entities.Users;
import com.rishav.Chatty.enums.Status;
import com.rishav.Chatty.repo.ContactRepo;
import com.rishav.Chatty.repo.ContactRequestRepo;
import com.rishav.Chatty.repo.NotificationRepo;
import com.rishav.Chatty.repo.UsersRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ContactRequestService {

    @Autowired
    private ContactRequestRepo contactRequestRepository;

    @Autowired
    private UsersRepo userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ContactRepo contactRepository;
    @Autowired
    private NotificationRepo notificationRepository;

    /**
     * Handles sending a contact request by storing it in the database and sending a WebSocket notification.
     */
    @Transactional
    public String sendContactRequest(ContactRequestDTO dto) {
        Users sender = userRepository.findByEmail(dto.getSenderEmail());
        Users receiver = userRepository.findByEmail(dto.getReceiverEmail());

        if ((sender == null) || ( receiver == null)) {
            return "Sender or receiver not found.";
        }


        // Prevent duplicate requests
        if (contactRequestRepository.existsBySenderAndReceiver(sender, receiver)) {
            return "Contact request already sent.";
        }

        ContactRequest request = new ContactRequest();
        request.setSender(sender);
        request.setReceiver(receiver);
        request.setStatus(Status.PENDING);
        request.setRequestedAt(LocalDateTime.now());
        request = contactRequestRepository.save(request); // Save and get the generated ID

        // ✅ Save notification to DB
        Notification notification = new Notification();
        notification.setReceiver(receiver);
        notification.setMessage(String.format("You received a contact request from %s", sender.getUsername()));
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);
        notification= notificationRepository.save(notification);


        NotificationDTO notificationDTO=new NotificationDTO();
        notificationDTO.setId(notification.getId());
        notificationDTO.setMessage(notification.getMessage());
        notificationDTO.setCreatedAt(notification.getCreatedAt());
        notificationDTO.setRead(false);
        notificationDTO.setRequestId(request.getId()); // Include the contact request ID
        notificationDTO.setSenderEmail(sender.getEmail());
        notificationDTO.setSenderUsername(sender.getUsername());

        System.out.println("Sending notification with request ID: " + request.getId());

        // Notify the receiver via WebSocket
        messagingTemplate.convertAndSendToUser(
                receiver.getEmail(),
                "/queue/notifications",
                notificationDTO
        );

        return "Contact request sent.";
    }

    /**
     * Accepts a contact request and updates the status.
     */
    @Transactional
    public String acceptContactRequest(long requestId) {
        Optional<ContactRequest> requestOpt = contactRequestRepository.findById(requestId);
        if (requestOpt.isEmpty()) return "Contact request not found.";

        ContactRequest request = requestOpt.get();

        if (request.getStatus() == Status.ACCEPTED) return "Request already accepted.";
        if (request.getStatus() == Status.REJECTED) return "Request already rejected.";

        Users sender = request.getSender();
        Users receiver = request.getReceiver();

        // Check if contact already exists
        boolean alreadyExists = contactRepository.existsByOwnerAndContact(sender, receiver)
                || contactRepository.existsByOwnerAndContact(receiver, sender);

        if (alreadyExists) {
            request.setStatus(Status.ACCEPTED);
            contactRequestRepository.save(request);
            contactRequestRepository.delete(request);
            return "You are already contacts.";
        }

        // Save contacts both ways
        Contact contactForSender = new Contact();
        contactForSender.setOwner(sender);
        contactForSender.setContact(receiver);

        Contact contactForReceiver = new Contact();
        contactForReceiver.setOwner(receiver);
        contactForReceiver.setContact(sender);

        contactRepository.save(contactForSender);
        contactRepository.save(contactForReceiver);

        // Update request status
        request.setStatus(Status.ACCEPTED);
        contactRequestRepository.save(request);
        contactRequestRepository.delete(request);

        // 🗑️ Delete the original contact request notification from the database
        // Find and remove the notification that was sent when the request was originally created
        Optional<Notification> originalNotification = notificationRepository
                .findByReceiverAndMessageContaining(receiver, "You received a contact request from " + sender.getUsername());
        
        if (originalNotification.isPresent()) {
            notificationRepository.delete(originalNotification.get());
            System.out.println("✅ Deleted original contact request notification from database");
        } else {
            System.out.println("⚠️ Original contact request notification not found in database");
        }

        // ✅ Save notification to DB for the sender (who sent the original request)
        Notification senderNotification = new Notification();
        senderNotification.setReceiver(sender);  // Notify the one who SENT the request
        senderNotification.setMessage(receiver.getUsername() + " accepted your contact request.");
        senderNotification.setCreatedAt(LocalDateTime.now());
        senderNotification.setRead(false);
        senderNotification = notificationRepository.save(senderNotification);

        // Create NotificationDTO for sender
        NotificationDTO senderNotificationDTO = new NotificationDTO();
        senderNotificationDTO.setId(senderNotification.getId());
        senderNotificationDTO.setMessage(senderNotification.getMessage());
        senderNotificationDTO.setCreatedAt(senderNotification.getCreatedAt());
        senderNotificationDTO.setRead(false);
        senderNotificationDTO.setSenderEmail(receiver.getEmail());
        senderNotificationDTO.setSenderUsername(receiver.getUsername());

        // 📣 Send WebSocket notification to sender
        messagingTemplate.convertAndSendToUser(
                sender.getEmail(),
                "/queue/notifications",
                senderNotificationDTO
        );

        // ✅ Send confirmation notification to receiver (who accepted)
        NotificationDTO receiverNotificationDTO = new NotificationDTO();
        receiverNotificationDTO.setRequestId(request.getId()); // Use the original request ID for updating
        receiverNotificationDTO.setMessage("You have accepted the contact request from " + sender.getUsername());
        receiverNotificationDTO.setCreatedAt(LocalDateTime.now());
        receiverNotificationDTO.setRead(false);
        receiverNotificationDTO.setSenderEmail(sender.getEmail());
        receiverNotificationDTO.setSenderUsername(sender.getUsername());

        // 📣 Send WebSocket update to receiver
        messagingTemplate.convertAndSendToUser(
                receiver.getEmail(),
                "/queue/notifications",
                receiverNotificationDTO
        );

        return "Contact request accepted. You are now contacts!";
    }

    /**
     * Rejects a contact request and updates the status.
     */
    @Transactional
    public String rejectContactRequest(long requestId) {
        Optional<ContactRequest> requestOpt = contactRequestRepository.findById(requestId);
        if (requestOpt.isEmpty()) return "Contact request not found.";

        ContactRequest request = requestOpt.get();

        if (request.getStatus() == Status.ACCEPTED) return "Request already accepted.";
        if (request.getStatus() == Status.REJECTED) return "Request already rejected.";

        Users sender = request.getSender();
        Users receiver = request.getReceiver();

        // Update request status to rejected
        request.setStatus(Status.REJECTED);
        contactRequestRepository.save(request);
        contactRequestRepository.delete(request);

        // ✅ Save notification to DB
        Notification notification = new Notification();
        notification.setReceiver(sender);  // Notify the one who SENT the request
        notification.setMessage(receiver.getUsername() + " declined your contact request.");
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);
        notification = notificationRepository.save(notification);

        // Create NotificationDTO for WebSocket
        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setId(notification.getId());
        notificationDTO.setMessage(notification.getMessage());
        notificationDTO.setCreatedAt(notification.getCreatedAt());
        notificationDTO.setRead(false);
        notificationDTO.setSenderEmail(receiver.getEmail());
        notificationDTO.setSenderUsername(receiver.getUsername());

        // 📣 Send WebSocket notification to sender
        messagingTemplate.convertAndSendToUser(
                sender.getEmail(),
                "/queue/notifications",
                notificationDTO
        );

        return "Contact request rejected.";
    }


    // You can add methods like rejectContactRequest(), getPendingRequestsForUser(), etc.
}
