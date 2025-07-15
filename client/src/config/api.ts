// API configuration for local development
export const API_CONFIG = {
  // API Gateway (all REST API calls go through this)
  BASE_URL: 'http://localhost:8081',
  
  // Direct service connections for WebSocket
  CHAT_WS_URL: 'http://localhost:8085',
  NOTIFICATION_WS_URL: 'http://localhost:8086',
  
  // Endpoints
  ENDPOINTS: {
    // Auth endpoints
    AUTH: {
      LOGIN: '/auth/login',
      REGISTER: '/auth/register',
      LOGOUT: '/auth/logout',
      REFRESH: '/auth/refresh'
    },
    
    // User endpoints
    USER: {
      SEARCH: '/user/searchUsers',
      PROFILE: '/user/profile',
      UPDATE: '/user/update'
    },
    
    // Chat endpoints  
    CHAT: {
      ALL_CHATS: '/chat/allChats',
      MESSAGES: '/messages/between',
      SEND_MESSAGE: '/chat/send'
    },
    
    // Notification endpoints
    NOTIFICATION: {
      ALL: '/notf/notifications',
      MARK_READ: '/notf/markRead'
    },
    
    // File endpoints
    FILE: {
      UPLOAD: '/files/upload'
    },
    
    // SSE endpoints
    SSE: {
      HEALTH: '/sse/health',
      UPDATES: '/sse/updates'
    },
    
    // Presence endpoints
    PRESENCE: {
      ONLINE: '/presence/online',
      OFFLINE: '/presence/offline'
    }
  },
  
  // WebSocket paths
  WEBSOCKET: {
    CHAT: '/ws',
    NOTIFICATION: '/ws'
  }
};

// Helper function to build full API URL
export const buildApiUrl = (endpoint: string): string => {
  return `${API_CONFIG.BASE_URL}${endpoint}`;
};

// Helper function to build WebSocket URL
export const buildWebSocketUrl = (service: 'chat' | 'notification', token: string): string => {
  const baseUrl = service === 'chat' ? API_CONFIG.CHAT_WS_URL : API_CONFIG.NOTIFICATION_WS_URL;
  const path = API_CONFIG.WEBSOCKET[service.toUpperCase() as keyof typeof API_CONFIG.WEBSOCKET];
  return `${baseUrl}${path}?token=${token}`;
};

export default API_CONFIG;
