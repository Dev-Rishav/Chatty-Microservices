package com.chatty.chatservice.events;

/**
 * Event for message updates
 */
public class MessageUpdateEvent {
    private final String senderId;
    private final String receiverId;
    private final Object messageData;

    public MessageUpdateEvent(String senderId, String receiverId, Object messageData) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.messageData = messageData;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public Object getMessageData() {
        return messageData;
    }
}
