package com.chatty.notificationservice.service;


import com.chatty.notificationservice.dto.ContactRequestDTO;
import com.chatty.notificationservice.dto.NotificationDTO;
import com.chatty.notificationservice.dto.Users;
import com.chatty.notificationservice.entity.Contact;
import com.chatty.notificationservice.entity.ContactRequest;
import com.chatty.notificationservice.entity.Notification;
import com.chatty.notificationservice.enums.Status;
import com.chatty.notificationservice.repo.ContactRepo;
import com.chatty.notificationservice.repo.ContactRequestRepo;
import com.chatty.notificationservice.repo.NotificationRepo;
import com.chatty.protos.auth.AuthServiceGrpc;
import com.chatty.user.grpc.GetUserByEmailRequest;
import com.chatty.user.grpc.UserResponse;
import com.chatty.user.grpc.UserServiceGrpc;
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
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private ContactRepo contactRepository;
    @Autowired
    private NotificationRepo notificationRepository;
    @Autowired
    private UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub;

    /**
     * Handles sending a contact request by storing it in the database and sending a WebSocket notification.
     */
    @Transactional
    public String sendContactRequest(ContactRequestDTO dto) {

        System.out.println("Sending contact request");
        GetUserByEmailRequest senderReq=GetUserByEmailRequest.newBuilder().setEmail(dto.getSenderEmail()).build();
        UserResponse senderRes=userServiceBlockingStub.getUserByEmail(senderReq);

        Users sender = new Users();
        sender.setEmail(senderRes.getEmail());
        sender.setUsername(senderRes.getUsername());
        sender.setProfile_pic(senderRes.getProfilePic());
        sender.setUser_id(senderRes.getUserId());


        GetUserByEmailRequest receiverReq=GetUserByEmailRequest.newBuilder().setEmail(dto.getReceiverEmail()).build();
        UserResponse receiverRes=userServiceBlockingStub.getUserByEmail(receiverReq);

        Users receiver = new Users();
        receiver.setEmail(receiverRes.getEmail());
        receiver.setUsername(receiverRes.getUsername());
        receiver.setProfile_pic(receiverRes.getProfilePic());
        receiver.setUser_id(receiverRes.getUserId());

        if ((sender.getEmail() == null) || ( receiver.getEmail() == null)) {
            return "Sender or receiver not found.";
        }


        // Prevent duplicate requests
        if (contactRequestRepository.existsBySenderAndReceiver(sender.getEmail(), receiver.getEmail())) {
            return "Contact request already sent.";
        }

        ContactRequest request = new ContactRequest();
        request.setSender(sender.getEmail());
        request.setReceiver(receiver.getEmail());
        request.setStatus(Status.PENDING);
        request.setRequestedAt(LocalDateTime.now());
        request = contactRequestRepository.save(request); // Save and get the generated ID



        // ✅ Save notification to DB
        Notification notification = new Notification();
        notification.setReceiver(receiver.getEmail());
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
        System.out.println(receiver.getEmail()+"receiving the notification");

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
        try {
            System.out.println("Accepting contact request with ID: " + requestId);
            Optional<ContactRequest> requestOpt = contactRequestRepository.findById(requestId);
            if (requestOpt.isEmpty()) {
                System.out.println("Contact request not found for ID: " + requestId);
                return "Contact request not found.";
            }

            ContactRequest request = requestOpt.get();
            System.out.println("Found contact request: " + request.getSender() + " -> " + request.getReceiver());

            if (request.getStatus() == Status.ACCEPTED) return "Request already accepted.";
            if (request.getStatus() == Status.REJECTED) return "Request already rejected.";

            //Grpc user stub injection
            GetUserByEmailRequest senderReq=GetUserByEmailRequest.newBuilder().setEmail(request.getSender()).build();
            GetUserByEmailRequest receiverReq=GetUserByEmailRequest.newBuilder().setEmail(request.getReceiver()).build();

            UserResponse senderRes=userServiceBlockingStub.getUserByEmail(senderReq);
            UserResponse receiverRes=userServiceBlockingStub.getUserByEmail(receiverReq);

            Users sender = new Users();
            sender.setEmail(senderRes.getEmail());
            sender.setUsername(senderRes.getUsername());
            sender.setProfile_pic(senderRes.getProfilePic());
            sender.setUser_id(senderRes.getUserId()); // Remove the (int) cast
            Users receiver = new Users();
            receiver.setEmail(receiverRes.getEmail());
            receiver.setUsername(receiverRes.getUsername());
            receiver.setProfile_pic(receiverRes.getProfilePic());
            receiver.setUser_id(receiverRes.getUserId()); // Remove the (int) cast

            System.out.println("Retrieved users - Sender: " + sender.getEmail() + ", Receiver: " + receiver.getEmail());

            // Check if contact already exists
            boolean alreadyExists = contactRepository.existsByOwnerAndContact(sender.getEmail(), receiver.getEmail())
                    || contactRepository.existsByOwnerAndContact(receiver.getEmail(), sender.getEmail());

            if (alreadyExists) {
                System.out.println("Contacts already exist");
                request.setStatus(Status.ACCEPTED);
                contactRequestRepository.save(request);
                contactRequestRepository.delete(request);
                return "You are already contacts.";
            }

            System.out.println("Creating new contacts...");
            // Save contacts both ways
            Contact contactForSender = new Contact();
            contactForSender.setOwner(sender.getEmail());
            contactForSender.setContact(receiver.getEmail());

            Contact contactForReceiver = new Contact();
            contactForReceiver.setOwner(receiver.getEmail());
            contactForReceiver.setContact(sender.getEmail());

            contactRepository.save(contactForSender);
            contactRepository.save(contactForReceiver);
            System.out.println("Contacts saved successfully");

            // Update request status
            request.setStatus(Status.ACCEPTED);
            contactRequestRepository.save(request);
            contactRequestRepository.delete(request);
            System.out.println("Contact request updated and removed");

            // 🗑️ Delete the original contact request notification from the database
            // Find and remove the notification that was sent when the request was originally created
            Optional<Notification> originalNotification = notificationRepository
                    .findByReceiverAndMessageContaining(receiver.getEmail(), "You received a contact request from " + sender.getUsername());
            
            if (originalNotification.isPresent()) {
                notificationRepository.delete(originalNotification.get());
                System.out.println("✅ Deleted original contact request notification from database");
            } else {
                System.out.println("⚠️ Original contact request notification not found in database");
            }

            // ✅ Save notification to DB for the sender (who sent the original request)
            Notification senderNotification = new Notification();
            senderNotification.setReceiver(sender.getEmail());  // Notify the one who SENT the request
            senderNotification.setMessage(receiver.getUsername() + " accepted your contact request.");
            senderNotification.setCreatedAt(LocalDateTime.now());
            senderNotification.setRead(false);
            senderNotification = notificationRepository.save(senderNotification);
            System.out.println("Sender notification saved with ID: " + senderNotification.getId());



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
            System.out.println("WebSocket notification sent to sender: " + sender.getEmail());

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
            System.out.println("WebSocket notification sent to receiver: " + receiver.getEmail());

            return "Contact request accepted. You are now contacts!";
        } catch (Exception e) {
            System.err.println("Error accepting contact request: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw to trigger rollback
        }
    }

    /**
     * Rejects a contact request and updates the status.
     */
    @Transactional
    public String rejectContactRequest(long requestId) {
        System.out.println("Rejecting contact request");
        Optional<ContactRequest> requestOpt = contactRequestRepository.findById(requestId);
        if (requestOpt.isEmpty()) return "Contact request not found.";

        ContactRequest request = requestOpt.get();

        if (request.getStatus() == Status.ACCEPTED) return "Request already accepted.";
        if (request.getStatus() == Status.REJECTED) return "Request already rejected.";

        //Grpc user stub injection
        GetUserByEmailRequest senderReq=GetUserByEmailRequest.newBuilder().setEmail(request.getSender()).build();
        GetUserByEmailRequest receiverReq=GetUserByEmailRequest.newBuilder().setEmail(request.getReceiver()).build();

        UserResponse senderRes=userServiceBlockingStub.getUserByEmail(senderReq);
        UserResponse receiverRes=userServiceBlockingStub.getUserByEmail(receiverReq);

        Users sender = new Users();
        sender.setEmail(senderRes.getEmail());
        sender.setUsername(senderRes.getUsername());
        sender.setProfile_pic(senderRes.getProfilePic());
        sender.setUser_id(senderRes.getUserId());
        
        Users receiver = new Users();
        receiver.setEmail(receiverRes.getEmail());
        receiver.setUsername(receiverRes.getUsername());
        receiver.setProfile_pic(receiverRes.getProfilePic());
        receiver.setUser_id(receiverRes.getUserId());

        // Update request status to rejected
        request.setStatus(Status.REJECTED);
        contactRequestRepository.save(request);
        contactRequestRepository.delete(request);

        // ✅ Save notification to DB for sender (who sent the original request)
        Notification senderNotification = new Notification();
        senderNotification.setReceiver(sender.getEmail());  // Notify the one who SENT the request
        senderNotification.setMessage(receiver.getUsername() + " declined your contact request.");
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

        // Note: We're NOT sending any update to the receiver for rejection
        // The original notification stays visible for them

        return "Contact request rejected.";
    }


    // You can add methods like rejectContactRequest(), getPendingRequestsForUser(), etc.
}

