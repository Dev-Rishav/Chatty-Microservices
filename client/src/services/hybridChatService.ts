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
  /**
   * Start active chat session (connects WebSocket as indicator of open chat window)
   * WebSocket connection itself signals to backend that chat window is open
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

      console.log("💬 Starting chat session with:", receiverId);
      console.log("🔌 Connecting WebSocket (signals backend: chat window open)");

      // Connect WebSocket - this connection itself tells backend chat window is open
      chatStompService.connect(this.token, () => {
        console.log("✅ WebSocket connected - Backend now knows chat window is open");
        
        // Subscribe to private messages for this session
        chatStompService.subscribe("/user/queue/messages", (message: any) => {
          if (this.activeChatSession && this.activeChatSession.receiverId === message.from) {
            // Message from the current chat partner
            console.log("📨 Received WebSocket message from active chat:", message.from);
            this.activeChatSession.onMessageReceived?.(message);
          } else {
            // Message from someone else - these will be handled via SSE background updates
            console.log("📨 Background message from:", message.from, "(will be handled via SSE)");
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
   * End active chat session (disconnects WebSocket - signals backend chat window closed)
   * WebSocket disconnection itself tells backend that chat window is closed
   */
  endChatSession() {
    if (this.activeChatSession) {
      console.log("💬 Ending chat session with:", this.activeChatSession.receiverId);
      console.log("🔌 Disconnecting WebSocket (signals backend: chat window closed)");
      this.activeChatSession = null;
    }

    // Disconnect WebSocket - this disconnection tells backend chat window is closed
    if(chatStompService.isConnected()) {
      console.log("✅ WebSocket disconnected - Backend now knows chat window is closed");
      chatStompService.disconnect();
    }
    
    // SSE remains connected for background updates
    console.log("📡 SSE remains connected for background notifications");
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

  // =============================================================================
  // OPTIMIZATION COMPLETE ✅
  // WebSocket connection/disconnection now automatically manages chat sessions
  // No need for explicit API calls - backend detects connection state
  // =============================================================================
}

export default HybridChatService.getInstance();
