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
        if (contactRequestRepository.existsBySenderAndReceiver(sender, receiver)) {
            return "Contact request already sent.";
        }

        ContactRequest request = new ContactRequest();
        request.setSender(sender.getEmail());
        request.setReceiver(receiver.getEmail());
        request.setStatus(Status.PENDING);
        request.setRequestedAt(LocalDateTime.now());
        contactRequestRepository.save(request);

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
        contactForSender.setOwner(sender.getEmail());
        contactForSender.setContact(receiver.getEmail());

        Contact contactForReceiver = new Contact();
        contactForReceiver.setOwner(receiver.getEmail());
        contactForReceiver.setContact(sender.getEmail());

        contactRepository.save(contactForSender);
        contactRepository.save(contactForReceiver);

        // Update request status
        request.setStatus(Status.ACCEPTED);
        contactRequestRepository.save(request);
        contactRequestRepository.delete(request);

        // ✅ Save notification to DB
        Notification notification = new Notification();
        notification.setReceiver(sender.getEmail());  // Notify the one who SENT the request
        notification.setMessage(receiver.getUsername() + " accepted your contact request.");
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);
        notificationRepository.save(notification);

        // 📣 Send WebSocket notification to sender
        messagingTemplate.convertAndSendToUser(
                sender.getEmail(),
                "/queue/notifications",
                notification
        );

        return "Contact request accepted. You are now contacts!";
    }


    // You can add methods like rejectContactRequest(), getPendingRequestsForUser(), etc.
}

