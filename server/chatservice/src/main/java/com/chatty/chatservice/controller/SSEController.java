package com.chatty.chatservice.controller;

import com.chatty.chatservice.dto.ChatDTO;
import com.chatty.chatservice.events.MessageUpdateEvent;
import com.chatty.chatservice.events.PresenceUpdateEvent;
import com.chatty.chatservice.events.TypingStatusEvent;
import com.chatty.chatservice.service.ActiveChatSessionService;
import com.chatty.chatservice.service.ChatService;
import com.chatty.chatservice.service.JWTService;
import com.chatty.chatservice.service.OnlineUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/sse")
//@CrossOrigin(origins = "*")
public class SSEController {

    @Autowired
    private OnlineUserService onlineUserService;

    @Autowired
    private ChatService chatService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JWTService jWTService;

    @Autowired
    private ActiveChatSessionService activeChatSessionService;

    private final ConcurrentHashMap<String, SseEmitter> userEmitters = new ConcurrentHashMap<>();

    /**
     * SSE endpoint for real-time presence and chat updates
     */
    @GetMapping(value = "/updates", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamUpdates(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "token", required = false) String tokenParam) {

        String token = null;
        System.out.println("token: " + authHeader);

        // Try to get token from header first, then from query param
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else if (tokenParam != null) {
            token = tokenParam;
        }

        if (token == null) {
            throw new RuntimeException("No authentication token provided");
        }

//        String userId = sseService.getUserIdFromToken(token);

        String userId = jWTService.extractUsername(token);
        if (userId == null) {
            throw new RuntimeException("Invalid token");
        }

        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE); // No timeout
        userEmitters.put(userId, emitter);

        // Handle cleanup on completion/timeout/error
        emitter.onCompletion(() -> {
            userEmitters.remove(userId);
            System.out.println("🔌 SSE connection completed for user: " + userId);
        });

        emitter.onTimeout(() -> {
            userEmitters.remove(userId);
            System.out.println("⏰ SSE connection timeout for user: " + userId);
        });

        emitter.onError((ex) -> {
            userEmitters.remove(userId);
            System.out.println("❌ SSE connection error for user: " + userId + " - " + ex.getMessage());
        });

        try {
            // Send initial data
            sendInitialData(emitter, token);
            System.out.println("📡 SSE connection established for user: " + userId);
        } catch (IOException e) {
            System.err.println("Failed to send initial SSE data: " + e.getMessage());
            emitter.completeWithError(e);
        }

        return emitter;
    }

    /**
     * Send initial data to newly connected client
     */
    private void sendInitialData(SseEmitter emitter, String token) throws IOException {
        // Send online users
        Set<String> onlineUsers = onlineUserService.getOnlineUsers();
        SSEEventData presenceData = new SSEEventData("presence_update", onlineUsers);
        emitter.send(SseEmitter.event()
                .name("presence_update")
                .data(objectMapper.writeValueAsString(presenceData)));

        // Send chat list with last messages
        List<ChatDTO> chats = chatService.getAllChats(token);
        SSEEventData chatData = new SSEEventData("chat_list_update", chats);
        emitter.send(SseEmitter.event()
                .name("chat_list_update")
                .data(objectMapper.writeValueAsString(chatData)));
    }

    /**
     * Broadcast presence update to all connected users
     */
    public void broadcastPresenceUpdate(Set<String> onlineUsers) {
        SSEEventData eventData = new SSEEventData("presence_update", onlineUsers);
        broadcastToAll("presence_update", eventData);
    }

    /**
     * Broadcast new message update to specific users
     */
    public void broadcastMessageUpdate(String senderId, String receiverId, Object messageData) {
        SSEEventData eventData = new SSEEventData("message_update", messageData);

        // Send to both sender and receiver
        sendToUser(senderId, "message_update", eventData);
        sendToUser(receiverId, "message_update", eventData);
    }

    /**
     * Broadcast chat list update to a specific user
     */
    public void broadcastChatListUpdate(String userId, List<ChatDTO> chats) {
        SSEEventData eventData = new SSEEventData("chat_list_update", chats);
        sendToUser(userId, "chat_list_update", eventData);
    }

    /**
     * Send event to specific user
     */
    private void sendToUser(String userId, String eventName, SSEEventData data) {
        SseEmitter emitter = userEmitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(objectMapper.writeValueAsString(data)));
            } catch (IOException e) {
                System.err.println("Failed to send SSE event to user " + userId + ": " + e.getMessage());
                userEmitters.remove(userId);
            }
        }
    }

    /**
     * Broadcast to all connected users
     */
    private void broadcastToAll(String eventName, SSEEventData data) {
        userEmitters.entrySet().removeIf(entry -> {
            try {
                entry.getValue().send(SseEmitter.event()
                        .name(eventName)
                        .data(objectMapper.writeValueAsString(data)));
                return false;
            } catch (IOException e) {
                System.err.println("Failed to broadcast SSE event: " + e.getMessage());
                return true; // Remove failed emitter
            }
        });
    }

    /**
     * Get number of active SSE connections (for testing)
     */
    @GetMapping("/connections/count")
    public ResponseEntity<Integer> getActiveConnections() {
        return ResponseEntity.ok(userEmitters.size());
    }

    /**
     * Get list of connected users (for testing)
     */
    @GetMapping("/connections/users")
    public ResponseEntity<Set<String>> getConnectedUsers() {
        return ResponseEntity.ok(userEmitters.keySet());
    }

    /**
     * Test endpoint to manually trigger presence update (for testing)
     */
    @PostMapping("/test/presence")
    public ResponseEntity<String> testPresenceUpdate(@RequestBody Set<String> onlineUsers) {
        broadcastPresenceUpdate(onlineUsers);
        return ResponseEntity.ok("Presence update sent to " + userEmitters.size() + " connected users");
    }

    /**
     * Test endpoint to manually trigger message update (for testing)
     */
    @PostMapping("/test/message")
    public ResponseEntity<String> testMessageUpdate(
            @RequestParam String senderId,
            @RequestParam String receiverId,
            @RequestParam String content) {

        TestMessage testMessage = new TestMessage(senderId, receiverId, content);
        broadcastMessageUpdate(senderId, receiverId, testMessage);

        return ResponseEntity.ok("Message update sent from " + senderId + " to " + receiverId);
    }

    /**
     * Test endpoint to manually trigger typing status (for testing)
     */
    @PostMapping("/test/typing")
    public ResponseEntity<String> testTypingStatus(
            @RequestParam String senderId,
            @RequestParam String receiverId,
            @RequestParam boolean isTyping) {

        TypingStatusData typingData = new TypingStatusData(senderId, receiverId, isTyping);
        broadcastMessageUpdate(senderId, receiverId, typingData);

        return ResponseEntity.ok("Typing status sent: " + senderId + " -> " + receiverId + " (typing: " + isTyping + ")");
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("activeConnections", userEmitters.size());
        health.put("connectedUsers", userEmitters.keySet());
        health.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(health);
    }

    /**
     * Mark chat window as opened (called when user opens a chat)
     */
    @PostMapping("/chat/open")
    public ResponseEntity<String> openChatWindow(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String chatPartnerId) {

        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        String userId = jWTService.extractUsername(token);

        if (userId == null) {
            return ResponseEntity.badRequest().body("Invalid token");
        }

        activeChatSessionService.openChatSession(userId, chatPartnerId);
        return ResponseEntity.ok("Chat window opened: " + userId + " <-> " + chatPartnerId);
    }

    /**
     * Mark chat window as closed (called when user closes a chat)
     */
    @PostMapping("/chat/close")
    public ResponseEntity<String> closeChatWindow(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String chatPartnerId) {

        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        String userId = jWTService.extractUsername(token);

        if (userId == null) {
            return ResponseEntity.badRequest().body("Invalid token");
        }

        activeChatSessionService.closeChatSession(userId, chatPartnerId);
        return ResponseEntity.ok("Chat window closed: " + userId + " <-> " + chatPartnerId);
    }

    /**
     * Get active chat sessions for testing
     */
    @GetMapping("/chat/active")
    public ResponseEntity<Map<String, Object>> getActiveChatSessions(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Map<String, Object> response = new HashMap<>();
        response.put("activeChatSessionCount", activeChatSessionService.getActiveChatSessionCount());
        response.put("activeUsers", activeChatSessionService.getAllActiveUsers());

        if (authHeader != null) {
            String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
            String userId = jWTService.extractUsername(token);
            if (userId != null) {
                response.put("userActiveSessions", activeChatSessionService.getActiveChatSessions(userId));
            }
        }

        return ResponseEntity.ok(response);
    }

    /**
     * SSE Event Data wrapper
     */
    public static class SSEEventData {
        private String type;
        private Object data;
        private long timestamp;

        public SSEEventData(String type, Object data) {
            this.type = type;
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }

        // Getters and setters
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }

    /**
     * Test message class for manual testing
     */
    public static class TestMessage {
        private String from;
        private String to;
        private String content;
        private long timestamp;

        public TestMessage(String from, String to, String content) {
            this.from = from;
            this.to = to;
            this.content = content;
            this.timestamp = System.currentTimeMillis();
        }

        // Getters and setters
        public String getFrom() { return from; }
        public void setFrom(String from) { this.from = from; }
        public String getTo() { return to; }
        public void setTo(String to) { this.to = to; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }

    /**
     * Typing status data class
     */
    public static class TypingStatusData {
        private String senderId;
        private String receiverId;
        private boolean isTyping;
        private long timestamp;

        public TypingStatusData(String senderId, String receiverId, boolean isTyping) {
            this.senderId = senderId;
            this.receiverId = receiverId;
            this.isTyping = isTyping;
            this.timestamp = System.currentTimeMillis();
        }

        // Getters and setters
        public String getSenderId() {
            return senderId;
        }

        public void setSenderId(String senderId) {
            this.senderId = senderId;
        }

        public String getReceiverId() {
            return receiverId;
        }

        public void setReceiverId(String receiverId) {
            this.receiverId = receiverId;
        }

        public boolean isTyping() {
            return isTyping;
        }

        public void setTyping(boolean typing) {
            isTyping = typing;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }

    // Event Listeners for SSE events

    /**
     * Handle presence update events
     */
    @EventListener
    public void handlePresenceUpdate(PresenceUpdateEvent event) {
        broadcastPresenceUpdate(event.getOnlineUsers());
    }

    /**
     * Handle message update events
     */
    @EventListener
    public void handleMessageUpdate(MessageUpdateEvent event) {
        broadcastMessageUpdate(event.getSenderId(), event.getReceiverId(), event.getMessageData());
    }

    /**
     * Handle typing status events
     */
    @EventListener
    public void handleTypingStatus(TypingStatusEvent event) {
        TypingStatusData typingData = new TypingStatusData(
                event.getSenderId(),
                event.getReceiverId(),
                event.isTyping()
        );
        broadcastMessageUpdate(event.getSenderId(), event.getReceiverId(), typingData);
    }
}
