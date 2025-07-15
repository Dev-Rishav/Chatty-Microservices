import * as Stomp from "stompjs";
import SockJS from "sockjs-client";
import { ContactRequestDTO } from "../interfaces/types";
import { buildWebSocketUrl } from "../config/api";

class NotificationStompService {
  private static instance: NotificationStompService;
  private stompClient: Stomp.Client | null = null;
  private connected = false;
  private subscriptions: { [destination: string]: Stomp.Subscription } = {};

  private constructor() {}

  static getInstance(): NotificationStompService {
    if (!NotificationStompService.instance) {
      NotificationStompService.instance = new NotificationStompService();
    }
    return NotificationStompService.instance;
  }

  isConnected(): boolean {
    return this.connected && !!this.stompClient?.connected;
  }

  connect(token: string, onConnect?: () => void) {
    if (this.connected) {
      console.log("🔌 Already connected to Notification Service.");
      onConnect?.();
      return;
    }

    const socket = new SockJS(buildWebSocketUrl('notification', token));
    this.stompClient = Stomp.over(socket);
    this.stompClient.debug = () => {}; // Disable logs

    this.stompClient.connect(
      {},
      (frame: any) => {
        this.connected = true;
        console.log("🔔 Notification WebSocket connected", frame);
        onConnect?.();
      },
      (error: any) => {
        console.error("Notification WebSocket connection error: ", error);
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

  sendContactRequest(contactRequest: ContactRequestDTO) {
    if (this.isConnected()) {
      this.send("/app/send-contact-request", contactRequest);
    } else {
      console.warn("Not connected to Notification WebSocket.");
    }
  }

  acceptContactRequest(requestId: string) {
    if (this.isConnected()) {
      this.send("/app/accept-contact-request", parseInt(requestId));
      console.log("🤝 Accepting contact request:", requestId);
    } else {
      console.warn("Not connected to Notification WebSocket.");
    }
  }

  rejectContactRequest(requestId: string) {
    if (this.isConnected()) {
      this.send("/app/reject-contact-request", parseInt(requestId));
      console.log("❌ Rejecting contact request:", requestId);
    } else {
      console.warn("Not connected to Notification WebSocket.");
    }
  }

  disconnect() {
    Object.values(this.subscriptions).forEach((sub) => sub.unsubscribe());
    this.subscriptions = {};
    this.stompClient?.disconnect(() => {
      this.connected = false;
      console.log("🔌 Notification WebSocket disconnected");
    });
  }
}

export default NotificationStompService.getInstance();
