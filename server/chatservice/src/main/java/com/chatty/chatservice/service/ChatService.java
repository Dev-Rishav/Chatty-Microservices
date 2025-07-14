package com.chatty.chatservice.service;




import com.chatty.chatservice.dto.ChatDTO;
import com.chatty.chatservice.dto.ChatMessageDTO;
import com.chatty.chatservice.entity.Chat;
import com.chatty.chatservice.entity.Message;
import com.chatty.chatservice.pojo.Users;
import com.chatty.chatservice.repo.ChatRepo;
import com.chatty.chatservice.repo.MessageRepo;
import com.chatty.notification.grpc.ContactInfo;

import com.chatty.notification.grpc.ContactServiceGrpc;
import com.chatty.notification.grpc.GetUserContactsRequest;
import com.chatty.notification.grpc.GetUserContactsResponse;
import com.chatty.protos.auth.AuthServiceGrpc;
import com.chatty.protos.auth.TokenRequest;
import com.chatty.protos.auth.TokenResponse;
import com.chatty.user.grpc.GetUserByEmailRequest;
import com.chatty.user.grpc.UserResponse;
import com.chatty.user.grpc.UserServiceGrpc;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

import static java.lang.Integer.parseInt;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageRepo messageRepository;
    private final ChatRepo chatRepo;


    private final AuthServiceGrpc.AuthServiceBlockingStub authServiceBlockingStub;
    private final UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub;
    private final ContactServiceGrpc.ContactServiceBlockingStub contactServiceBlockingStub ;



    @Transactional
    public ChatMessageDTO sendPrivateMessage(ChatMessageDTO message, String senderEmail) {

        // Get  receiver from the DTO
        String receiverEmail = message.getTo();

        // Consistent ordering to prevent duplicate chats
        String user1Id = senderEmail;
        String user2Id = receiverEmail;
        if (user1Id.compareTo(user2Id) > 0) {
            String temp = user1Id;
            user1Id = user2Id;
            user2Id = temp;
        }
        GetUserByEmailRequest senderEmailReq= GetUserByEmailRequest.newBuilder().setEmail(senderEmail).build();
        GetUserByEmailRequest receiverEmailReq= GetUserByEmailRequest.newBuilder().setEmail(receiverEmail).build();

        // Fetch sender and receiver User entities from the UserRepository
        UserResponse senderRes = userServiceBlockingStub.getUserByEmail(senderEmailReq);
        UserResponse receiverRes = userServiceBlockingStub.getUserByEmail(receiverEmailReq);

        String sender=senderRes.getEmail();
        String receiver=receiverRes.getEmail();

        // Try to find existing chat with consistent ordering
        Chat chat = chatRepo.findByUser1IdAndUser2Id(user1Id, user2Id);

        // If no chat exists, create a new chat
        if (chat == null) {
            chat = new Chat();
            chat.setUser1Id(user1Id);
            chat.setUser2Id(user2Id);
            chat.setCreatedAt(LocalDateTime.now());
            chat=chatRepo.save(chat);
        }




        // Save the message to the database
        Message msg = new Message();
        msg.setSender(sender);
        msg.setReceiver(receiver);
        msg.setContent(message.getContent());
        msg.setChat(chat);
        msg.setFile_url(message.getFileUrl());
        msg=messageRepository.save(msg);

        // Populate response DTO
        ChatMessageDTO chatMessageDTO = new ChatMessageDTO();
        chatMessageDTO.setId(msg.getId());
        chatMessageDTO.setTo(receiverEmail);
        chatMessageDTO.setFrom(senderEmail);  // sender is the email from the principal
        chatMessageDTO.setContent(message.getContent());
        chatMessageDTO.setTimestamp(msg.getTimestamp());
        chatMessageDTO.setFileUrl(msg.getFile_url());

        // Send the message to the recipient via WebSocket
        messagingTemplate.convertAndSendToUser(
                receiverEmail,
                "/queue/messages",
                chatMessageDTO
        );

        System.out.println("Message saved and sent: " + msg);
        
        // Return the saved message DTO for SSE notification
        return chatMessageDTO;
    }

    @Transactional
    public List<ChatDTO> getAllChats(String token) {
        // Step 1: Validate token and get current user
        TokenResponse res = authServiceBlockingStub.validateToken(
                TokenRequest.newBuilder().setToken(token).build()
        );
        String currentUserEmail = res.getEmail();

        // Step 2: Get all contacts
        GetUserContactsResponse grpcResponse = contactServiceBlockingStub.getUserContacts(
                GetUserContactsRequest.newBuilder().setUserEmail(currentUserEmail).build()
        );
        List<ContactInfo> contacts = grpcResponse.getContactsList();

        if (contacts.isEmpty()) return Collections.emptyList();

        // Step 3: Extract all contact emails
        List<String> contactEmails = new ArrayList<>();
        for (ContactInfo contact : contacts) {
            contactEmails.add(contact.getContactEmail());
        }

        //extract the chatids from contactids - only for contacts with existing chats
        List<Integer> chatIds = chatRepo.findChatIdsWithUsers(currentUserEmail, contactEmails);

        System.out.println("chatIds: " + chatIds);

        // Step 4: Fetch latest messages for these chats
        List<Message> latestMessages = messageRepository.findLatestMessagesForChats(chatIds);
        Map<Integer, Message> messageMap = new HashMap<>();
        for (Message msg : latestMessages) {
            messageMap.put(msg.getChat().getChatId(), msg);
        }

        System.out.println("messageMap: " + messageMap);

        // Step 5: Build ChatDTOs from ALL contacts (including those without chats)
        List<ChatDTO> chatList = new ArrayList<>();
        for (ContactInfo contact : contacts) {
            String otherUserEmail = contact.getContactEmail();

            // Fetch user details
            UserResponse userRes = userServiceBlockingStub.getUserByEmail(
                    GetUserByEmailRequest.newBuilder().setEmail(otherUserEmail).build()
            );

            // Find if this contact has a chat
            Integer chatId = null;
            Message message = null;
            for (Integer id : chatIds) {
                // Check if this chatId corresponds to this contact
                Chat chat = chatRepo.findById(id).orElse(null);
                if (chat != null && 
                    (chat.getUser1Id().equals(otherUserEmail) || chat.getUser2Id().equals(otherUserEmail))) {
                    chatId = id;
                    message = messageMap.get(id);
                    break;
                }
            }

            ChatDTO dto = new ChatDTO();
            dto.setId(chatId); // Will be null for contacts without chats
            dto.setEmail(userRes.getEmail());
            dto.setUsername(userRes.getUsername());
            dto.setProfilePic(userRes.getProfilePic());

            // Set message details if any
            if (message != null) {
                dto.setLastMessage(message.getContent());
                dto.setTimestamp(message.getTimestamp());
                dto.setFileUrl(message.getFile_url());
            } else {
                dto.setLastMessage(null); // No messages yet
                dto.setTimestamp(null);
                dto.setFileUrl(null);
            }

            chatList.add(dto);
        }

        return chatList;
    }

}