package com.rishav.Chatty.services;


import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.rishav.Chatty.dto.ChatDTO;
import com.rishav.Chatty.dto.ChatMessageDTO;
import com.rishav.Chatty.entities.Chat;
import com.rishav.Chatty.entities.Contact;
import com.rishav.Chatty.entities.Message;
import com.rishav.Chatty.entities.Users;
import com.rishav.Chatty.repo.ChatRepo;
import com.rishav.Chatty.repo.ContactRepo;
import com.rishav.Chatty.repo.MessageRepo;
import com.rishav.Chatty.repo.UsersRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ChatService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MessageRepo messageRepository;
    @Autowired
    private UsersRepo usersRepo;
    @Autowired
    private ChatRepo chatRepo;
    @Autowired
    private MessageRepo messageRepo;
    @Autowired
    private ContactRepo contactRepository;

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

        // Fetch sender and receiver User entities from the UserRepository
        Users sender = usersRepo.findByEmail(senderEmail);
        Users receiver = usersRepo.findByEmail(receiverEmail);

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
        Users currentUser = usersRepo.findByEmail(currentUserEmail);

        // Step 1: Get all contacts for the current user
        List<Contact> contacts = contactRepository.findByOwnerEmail(currentUserEmail);
        if (contacts.isEmpty()) return Collections.emptyList();

        // Step 2: Find all chat IDs involving current user (for messages)
        List<Integer> chatIds = chatRepo.findChatIdsByUserEmail(currentUserEmail);
        
        // Step 3: Fetch latest messages for existing chats
        Map<String, Message> latestMessagesMap = new HashMap<>();
        if (!chatIds.isEmpty()) {
            List<Message> latestMessages = messageRepo.findLatestMessagesForChats(chatIds);
            for (Message message : latestMessages) {
                Chat chat = message.getChat();
                String otherUserId = chat.getUser1Id().equals(currentUserEmail)
                        ? chat.getUser2Id()
                        : chat.getUser1Id();
                latestMessagesMap.put(otherUserId, message);
            }
        }

        // Step 4: Build DTOs for all contacts
        List<ChatDTO> chatList = new ArrayList<>();
        for (Contact contact : contacts) {
            Users contactUser = contact.getContact();
            String contactEmail = contactUser.getEmail();

            ChatDTO dto = new ChatDTO();
            dto.setEmail(contactEmail);
            dto.setUsername(contactUser.getUsername());
            dto.setProfilePic(contactUser.getProfilePic());

            // Check if there's a message history with this contact
            Message latestMessage = latestMessagesMap.get(contactEmail);
            if (latestMessage != null) {
                // Contact with message history
                dto.setId(latestMessage.getChat().getChatId());
                dto.setLastMessage(latestMessage.getContent());
                dto.setTimestamp(latestMessage.getTimestamp());
                dto.setFileUrl(latestMessage.getFile_url());
            } else {
                // Contact without message history - set default values
                dto.setId(null); // No chat ID yet
                dto.setLastMessage("Start a conversation"); // Default message
                dto.setTimestamp(null); // No timestamp
                dto.setFileUrl(null); // No file
            }

            chatList.add(dto);
        }

        // Sort by timestamp (nulls last for new contacts)
        chatList.sort((a, b) -> {
            if (a.getTimestamp() == null && b.getTimestamp() == null) return 0;
            if (a.getTimestamp() == null) return 1;
            if (b.getTimestamp() == null) return -1;
            return b.getTimestamp().compareTo(a.getTimestamp());
        });

        return chatList;
    }
}