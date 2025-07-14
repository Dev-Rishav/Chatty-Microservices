import sseService from './sseService';
import chatStompService from './chatStompService';

export interface ChatSession {
  isActive: boolean;
  receiverId: string;
  onMessageReceived?: (message: any) => void;
}

class HybridChatService {
  private static instance: HybridChatService;
  private activeChatSession: ChatSession | null = null;
  private token: string | null = null;

  private constructor() {}

  static getInstance(): HybridChatService {
    if (!HybridChatService.instance) {
      HybridChatService.instance = new HybridChatService();
    }
    return HybridChatService.instance;
  }

  /**
   * Initialize the service with user token
   * This starts SSE connection for background updates
   */
  initialize(token: string) {
    this.token = token;

    // sseService.health(token);
    
    // Start SSE for background updates (presence, last messages)
    const url = `http://localhost:8081/sse/updates`;
    sseService.connectWithHeaders(url,token);
    
    console.log("🚀 Hybrid Chat Service initialized");
  }

  /**
   * Subscribe to SSE events for background updates
   */
  onPresenceUpdate(callback: (users: string[]) => void) {
    sseService.addEventListener('presence_update', (data) => {
      if (data.type === 'presence_update') {
        callback(data.data);
      }
    });
  }

  onChatListUpdate(callback: (chats: any[]) => void) {
    sseService.addEventListener('chat_list_update', (data) => {
      if (data.type === 'chat_list_update') {
        callback(data.data);
      }
    });
  }

  onMessageUpdate(callback: (message: any) => void) {
    sseService.addEventListener('message_update', (data) => {
      if (data.type === 'message_update') {
        callback(data.data);
      }
    });
  }

  /**
   * Start active chat session (connects WebSocket for real-time messaging)
   */
  startChatSession(receiverId: string, onMessageReceived?: (message: any) => void): Promise<void> {
    return new Promise((resolve, reject) => {
      if (!this.token) {
        reject(new Error("Service not initialized"));
        return;
      }

      // End any existing chat session
      this.endChatSession();

      this.activeChatSession = {
        isActive: true,
        receiverId,
        onMessageReceived
      };

      // Notify backend that chat window is opened
      this.notifyChatWindowOpened(receiverId).catch(console.error);

      // Connect WebSocket for active messaging
      chatStompService.connect(this.token, () => {
        // Subscribe to private messages for this session
        chatStompService.subscribe("/user/queue/messages", (message: any) => {
          if (this.activeChatSession && this.activeChatSession.receiverId === message.from) {
            // Message from the current chat partner
            this.activeChatSession.onMessageReceived?.(message);
          } else {
            // Message from someone else - handle via SSE or show notification
            console.log("📨 Message from other user:", message.from);
          }
        });

        console.log("💬 Active chat session started with:", receiverId);
        resolve();
      });
    });
  }

  /**
   * Send message (requires active chat session)
   */
  sendMessage(content: string, fileUrl?: string): Promise<void> {
    return new Promise((resolve, reject) => {
      if (!this.activeChatSession?.isActive) {
        reject(new Error("No active chat session"));
        return;
      }

      if (!chatStompService.isConnected()) {
        reject(new Error("WebSocket not connected"));
        return;
      }

      try {
        chatStompService.send("/app/private-message", {
          content,
          to: this.activeChatSession.receiverId,
          fileUrl
        });
        resolve();
      } catch (error) {
        reject(error);
      }
    });
  }

  /**
   * Send typing indicator
   */
  sendTypingStatus(isTyping: boolean) {
    if (!this.activeChatSession?.isActive || !chatStompService.isConnected()) {
      return;
    }

    chatStompService.send("/app/typing", {
      receiverId: this.activeChatSession.receiverId,
      isTyping
    });
  }

  /**
   * End active chat session (disconnects WebSocket, keeps SSE)
   */
  endChatSession() {
    if (this.activeChatSession) {
      console.log("💬 Ending chat session with:", this.activeChatSession.receiverId);
      this.notifyChatWindowClosed(this.activeChatSession.receiverId).catch(console.error);
      this.activeChatSession = null;
    }

    if(chatStompService.isConnected())
    // Disconnect WebSocket but keep SSE
    chatStompService.disconnect();
  }

  /**
   * Get current active chat session info
   */
  getActiveChatSession(): ChatSession | null {
    return this.activeChatSession;
  }

  /**
   * Check if currently in an active chat session
   */
  isInActiveChat(receiverId?: string): boolean {
    if (!this.activeChatSession?.isActive) return false;
    if (receiverId) {
      return this.activeChatSession.receiverId === receiverId;
    }
    return true;
  }

  /**
   * Disconnect everything
   */
  disconnect() {
    
    this.endChatSession();
    sseService.disconnect();
    this.token = null;
    console.log("🔌 Hybrid Chat Service disconnected");
  }

  /**
   * Reconnect SSE (useful for error recovery)
   */
  reconnectSSE() {
    if (this.token) {
      sseService.disconnect();
    //   sseService.connect(this.token);
    }
  }

  /**
   * Get connection status
   */
  getConnectionStatus() {
    return {
      sse: sseService.isConnected(),
      webSocket: chatStompService.isConnected(),
      activeChatSession: this.activeChatSession?.isActive || false
    };
  }

  /**
   * Notify backend that chat window is opened
   */
  private async notifyChatWindowOpened(receiverId: string): Promise<void> {
    if (!this.token) return;
    
    try {
      const response = await fetch(`http://localhost:8081/sse/chat/open?chatPartnerId=${encodeURIComponent(receiverId)}`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${this.token}`,
          'Content-Type': 'application/json'
        }
      });
      
      if (response.ok) {
        console.log("📱 Notified backend: chat window opened with", receiverId);
      }
    } catch (error) {
      console.error("Failed to notify chat window opened:", error);
    }
  }

  /**
   * Notify backend that chat window is closed
   */
  private async notifyChatWindowClosed(receiverId: string): Promise<void> {
    if (!this.token) return;
    
    try {
      const response = await fetch(`http://localhost:8081/sse/chat/close?chatPartnerId=${encodeURIComponent(receiverId)}`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${this.token}`,
          'Content-Type': 'application/json'
        }
      });
      
      if (response.ok) {
        console.log("📱 Notified backend: chat window closed with", receiverId);
      }
    } catch (error) {
      console.error("Failed to notify chat window closed:", error);
    }
  }
}

export default HybridChatService.getInstance();
