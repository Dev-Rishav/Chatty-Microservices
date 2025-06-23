import * as Stomp from "stompjs";
import SockJS from "sockjs-client";

class ChatStompService {
  private static instance: ChatStompService;
  private stompClient: Stomp.Client | null = null;
  private connected = false;
  private subscriptions: { [destination: string]: Stomp.Subscription } = {};

  private constructor() {}

  static getInstance(): ChatStompService {
    if (!ChatStompService.instance) {
      ChatStompService.instance = new ChatStompService();
    }
    return ChatStompService.instance;
  }

  isConnected(): boolean {
    return this.connected && !!this.stompClient?.connected;
  }

  connect(token: string, onConnect?: () => void) {
    if (this.connected) {
      console.log("🔌 Already connected to Chat Service.");
      onConnect?.();
      return;
    }

    const socket = new SockJS(`http://localhost:8085/ws?token=${token}`);
    this.stompClient = Stomp.over(socket);
    this.stompClient.debug = () => {}; // disable debug logs

    this.stompClient.connect(
      {},
      (frame: any) => {
        this.connected = true;
        console.log("💬 Chat WebSocket connected", frame);
        onConnect?.();
      },
      (error: any) => {
        console.error("Chat WebSocket connection error: ", error);
      }
    );
  }

  subscribe(destination: string, callback: (message: any) => void) {
    if (!this.connected || !this.stompClient) {
      console.warn(`⚠️ Tried subscribing to ${destination} before connection.`);
      return;
    }

    if (this.subscriptions[destination]) {
      console.warn(`Already subscribed to ${destination}`);
      return;
    }

    const subscription = this.stompClient.subscribe(destination, (payload:any) => {
      const message = JSON.parse(payload.body);
      callback(message);
    });

    this.subscriptions[destination] = subscription;
  }

  unsubscribe(destination: string) {
    const subscription = this.subscriptions[destination];
    if (subscription) {
      subscription.unsubscribe();
      delete this.subscriptions[destination];
      console.log(`🔕 Unsubscribed from ${destination}`);
    }
  }

  send(destination: string, body: any) {
    this.stompClient?.send(destination, {}, JSON.stringify(body));
  }

  disconnect() {
    Object.values(this.subscriptions).forEach((sub) => sub.unsubscribe());
    this.subscriptions = {};
    this.stompClient?.disconnect(() => {
      this.connected = false;
      console.log("🔌 Chat WebSocket disconnected");
    });
  }
}

export default ChatStompService.getInstance();
