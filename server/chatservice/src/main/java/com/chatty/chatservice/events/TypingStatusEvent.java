package com.chatty.chatservice.events;

/**
 * Event for typing status updates
 */
public class TypingStatusEvent {
    private final String senderId;
    private final String receiverId;
    private final boolean isTyping;
    private final long timestamp;

    public TypingStatusEvent(String senderId, String receiverId, boolean isTyping) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.isTyping = isTyping;
        this.timestamp = System.currentTimeMillis();
    }

    public String getSenderId() {
        return senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public boolean isTyping() {
        return isTyping;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
