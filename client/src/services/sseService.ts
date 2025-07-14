// SSE Service for real-time updates (presence, last messages)
class SSEService {
  private static instance: SSEService;
  private eventSource: EventSource | null = null;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectDelay = 1000;
  private listeners: { [eventType: string]: Array<(data: any) => void> } = {};

  private constructor() {}

  static getInstance(): SSEService {
    if (!SSEService.instance) {
      SSEService.instance = new SSEService();
    }
    return SSEService.instance;
  }

async health(token: string) {
    const res = await fetch("http://localhost:8081/sse/health", {
        headers: {
            Authorization: `Bearer ${token}`
        }
    });

    const data = await res.json(); // or res.json() if JSON is returned
    console.log("Health:", data);
    return data;
}




  connect(token: string) {
    if (this.eventSource) {
      console.log("🔌 SSE already connected");
      return;
    }

    const url = `http://localhost:8081/sse/updates`;
    
    this.eventSource = new EventSource(url, {
      withCredentials: false,
    });

    // Add authorization header manually by closing and reopening with custom headers
    this.eventSource.close();
    
    // Use fetch to create SSE with custom headers
    this.connectWithHeaders(url, token);
  }



   async connectWithHeaders(url: string, token: string) {
    try {
      // Create a new EventSource with a URL that includes the token as a query parameter
      const urlWithToken = `${url}?token=${encodeURIComponent(token)}`;
      
      this.eventSource = new EventSource(urlWithToken);

      this.eventSource.onopen = () => {
        console.log("📡 SSE connected successfully");
        this.reconnectAttempts = 0;
      };

      this.eventSource.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data);
          this.notifyListeners(event.type || 'message', data);
        } catch (error) {
          console.error("Failed to parse SSE message:", error);
        }
      };

      this.eventSource.addEventListener('presence_update', (event: any) => {
        try {
          const data = JSON.parse(event.data);
          this.notifyListeners('presence_update', data);
        } catch (error) {
          console.error("Failed to parse presence update:", error);
        }
      });

      this.eventSource.addEventListener('chat_list_update', (event: any) => {
        try {
          const data = JSON.parse(event.data);
          this.notifyListeners('chat_list_update', data);
        } catch (error) {
          console.error("Failed to parse chat list update:", error);
        }
      });

      this.eventSource.addEventListener('message_update', (event: any) => {
        try {
          const data = JSON.parse(event.data);
          this.notifyListeners('message_update', data);
        } catch (error) {
          console.error("Failed to parse message update:", error);
        }
      });

      this.eventSource.onerror = (error) => {
        console.error("❌ SSE connection error:", error);
        this.handleReconnect(token);
      };

    } catch (error) {
      console.error("❌ SSE connection error:", error);
      this.handleReconnect(token);
    }
  }

  private async readSSEStream(reader: ReadableStreamDefaultReader<Uint8Array>) {
    const decoder = new TextDecoder();
    let buffer = '';

    try {
      while (true) {
        const { done, value } = await reader.read();
        
        if (done) {
          console.log("📡 SSE stream ended");
          break;
        }

        buffer += decoder.decode(value, { stream: true });
        const lines = buffer.split('\n');
        
        // Keep the last incomplete line in buffer
        buffer = lines.pop() || '';

        for (const line of lines) {
          this.processSSELine(line);
        }
      }
    } catch (error) {
      console.error("❌ SSE stream error:", error);
    } finally {
      reader.releaseLock();
    }
  }

  private processSSELine(line: string) {
    if (line.startsWith('event:')) {
      this.currentEventType = line.substring(6).trim();
    } else if (line.startsWith('data:')) {
      const data = line.substring(5).trim();
      if (data && this.currentEventType) {
        try {
          const parsedData = JSON.parse(data);
          this.notifyListeners(this.currentEventType, parsedData);
        } catch (error) {
          console.error("Failed to parse SSE data:", error);
        }
      }
    }
  }

  private currentEventType: string | null = null;

  private notifyListeners(eventType: string, data: any) {
    const listeners = this.listeners[eventType] || [];
    listeners.forEach(listener => {
      try {
        listener(data);
      } catch (error) {
        console.error(`Error in SSE listener for ${eventType}:`, error);
      }
    });
  }

  addEventListener(eventType: string, listener: (data: any) => void) {
    if (!this.listeners[eventType]) {
      this.listeners[eventType] = [];
    }
    this.listeners[eventType].push(listener);
  }

  removeEventListener(eventType: string, listener: (data: any) => void) {
    if (this.listeners[eventType]) {
      this.listeners[eventType] = this.listeners[eventType].filter(l => l !== listener);
    }
  }

  private handleReconnect(token: string) {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error("❌ Max SSE reconnection attempts reached");
      return;
    }

    this.reconnectAttempts++;
    const delay = this.reconnectDelay * Math.pow(2, this.reconnectAttempts - 1);
    
    console.log(`🔄 Attempting SSE reconnection ${this.reconnectAttempts}/${this.maxReconnectAttempts} in ${delay}ms`);
    
    setTimeout(() => {
      this.connect(token);
    }, delay);
  }

  disconnect() {
    if (this.eventSource) {
      this.eventSource.close();
      this.eventSource = null;
      console.log("🔌 SSE disconnected");
    }
    this.reconnectAttempts = 0;
  }

  isConnected(): boolean {
    return this.eventSource !== null && this.eventSource.readyState === EventSource.OPEN;
  }
}

export default SSEService.getInstance();
