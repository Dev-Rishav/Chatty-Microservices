package com.chatty.chatservice.events;

import java.util.Set;

/**
 * Event for presence updates
 */
public class PresenceUpdateEvent {
    private final Set<String> onlineUsers;

    public PresenceUpdateEvent(Set<String> onlineUsers) {
        this.onlineUsers = onlineUsers;
    }

    public Set<String> getOnlineUsers() {
        return onlineUsers;
    }
}
