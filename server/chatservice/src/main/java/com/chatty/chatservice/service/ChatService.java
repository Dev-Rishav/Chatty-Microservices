package com.chatty.chatservice.service;



import com.chatty.chatservice.dto.ChatDTO;
import com.chatty.chatservice.dto.ChatMessageDTO;
import com.chatty.chatservice.entity.Chat;
import com.chatty.chatservice.entity.Message;
import com.chatty.chatservice.pojo.Users;
import com.chatty.chatservice.repo.ChatRepo;
import com.chatty.chatservice.repo.MessageRepo;
import com.chatty.user.grpc.GetUserByEmailRequest;
import com.chatty.user.grpc.UserResponse;
import com.chatty.user.grpc.UserServiceGrpc;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ChatService {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageRepo messageRepository;
    private final ChatRepo chatRepo;



    private final UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub;

    public ChatService (SimpMessagingTemplate messagingTemplate, ChatRepo chatRepo, UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub,MessageRepo messageRepository) {
        this.userServiceBlockingStub = userServiceBlockingStub;
        this.messageRepository = messageRepository;
        this.messagingTemplate = messagingTemplate;
        this.chatRepo = chatRepo;
    }

    @Transactional
    public void sendPrivateMessage(ChatMessageDTO message, String senderEmail) {

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
    }

    public List<ChatDTO> getAllChats(Principal principal) {
        String currentUserEmail = principal.getName();

        // Step 1: Find all chat IDs involving current user
        List<Integer> chatIds = chatRepo.findChatIdsByUserEmail(currentUserEmail);
        if (chatIds.isEmpty()) return Collections.emptyList();

        // Step 2: Fetch latest message for each chat ID in a single query
        List<Message> latestMessages = messageRepository.findLatestMessagesForChats(chatIds);

        // Step 3: Build DTOs from messages
        List<ChatDTO> chatList = new ArrayList<>();
        for (Message message : latestMessages) {
            Chat chat = message.getChat();

            // Determine the other user in the chat
            String otherUserId = chat.getUser1Id().equals(currentUserEmail)
                    ? chat.getUser2Id()
                    : chat.getUser1Id();
            GetUserByEmailRequest otherUserEmailReq= GetUserByEmailRequest.newBuilder().setEmail(otherUserId).build();


            UserResponse userRes= userServiceBlockingStub.getUserByEmail(otherUserEmailReq);

            Users otherUser=new  Users();
            otherUser.setUserId(userRes.getUserId());
            otherUser.setEmail(userRes.getEmail());
            otherUser.setUsername(userRes.getUsername());

            ChatDTO dto = new ChatDTO();

            dto.setId(chat.getChatId());
            dto.setEmail(otherUser.getEmail());
            dto.setUsername(otherUser.getUsername());
            dto.setProfilePic(otherUser.getProfilePic());
            dto.setLastMessage(message.getContent());
            dto.setTimestamp(message.getTimestamp());
            dto.setFileUrl(message.getFile_url());

            chatList.add(dto);
        }

        return chatList;

    }
}