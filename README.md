# 💬 Chatty Microservices

<div align="center">
  <img src="https://img.shields.io/badge/Spring_Boot-3.5.0-6DB33F?style=for-the-badge&logo=spring&logoColor=white" alt="Spring Boot">
  <img src="https://img.shields.io/badge/React-18.3.1-61DAFB?style=for-the-badge&logo=react&logoColor=white" alt="React">
  <img src="https://img.shields.io/badge/TypeScript-5.8.3-3178C6?style=for-the-badge&logo=typescript&logoColor=white" alt="TypeScript">
  <img src="https://img.shields.io/badge/PostgreSQL-336791?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL">
  <img src="https://img.shields.io/badge/Apache_Kafka-231F20?style=for-the-badge&logo=apache-kafka&logoColor=white" alt="Apache Kafka">
  <img src="https://img.shields.io/badge/Nginx-009639?style=for-the-badge&logo=nginx&logoColor=white" alt="Nginx">
  <img src="https://img.shields.io/badge/gRPC-4285F4?style=for-the-badge&logo=google&logoColor=white" alt="gRPC">
  <img src="https://img.shields.io/badge/WebSocket-010101?style=for-the-badge&logo=websocket&logoColor=white" alt="WebSocket">
  <img src="https://img.shields.io/badge/SSE-FF6B6B?style=for-the-badge&logo=data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjQiIGhlaWdodD0iMjQiIHZpZXdCb3g9IjAgMCAyNCAyNCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHBhdGggZD0iTTEyIDJMMTMuMDkgOC4yNkwyMCA5TDEzLjA5IDE1Ljc0TDEyIDIyTDEwLjkxIDE1Ljc0TDQgOUwxMC45MSA4LjI2TDEyIDJaIiBmaWxsPSJ3aGl0ZSIvPgo8L3N2Zz4K" alt="Server-Sent Events">
</div>

<div align="center">
  <h3>🚀 A modern, scalable real-time chat application built with microservices architecture</h3>
  <p>Experience seamless messaging with enterprise-grade architecture, real-time communication, and beautiful UI</p>
</div>

## 🌟 Live Demo

**🎯 [chatty-microservices.vercel.app](https://chatty-microservices.vercel.app)**

## 📖 Table of Contents

- [Features](#-features)
- [Architecture](#-architecture)
- [Technology Stack](#-technology-stack)
- [Apache Kafka Integration](#-apache-kafka-integration)
- [Nginx Configuration](#-nginx-configuration)
- [Server-Sent Events (SSE)](#-server-sent-events-sse)
- [Redis Integration](#-redis-integration)
- [Microservices](#-microservices)
- [Getting Started](#-getting-started)
- [API Documentation](#-api-documentation)
- [Real-time Features](#-real-time-features)
- [Security](#-security)
- [Contributing](#-contributing)
- [License](#-license)

## ✨ Features

### 🔐 **Authentication & Security**
- JWT-based authentication with secure token validation
- User registration and login with password hashing
- Protected routes and API endpoints
- Session management with refresh tokens

### 💬 **Real-time Messaging**
- Instant message delivery with WebSocket connections
- One-to-one private conversations
- File sharing and media support
- Message status indicators (sent, delivered, read)
- Typing indicators and online presence

### 👥 **Contact Management**
- Add contacts by email or username
- Contact request system with accept/reject functionality
- Real-time contact list updates
- Search and discover users

### 🎨 **Modern UI/UX**
- Beautiful, responsive design with Tailwind CSS
- Dark/Light mode toggle
- Smooth animations with Framer Motion
- Mobile-first responsive layout
- Paper-inspired design system

### 🏗️ **Enterprise Architecture**
- Microservices architecture with Spring Boot
- Service discovery with Eureka
- API Gateway for centralized routing
- gRPC for efficient inter-service communication
- Real-time updates with WebSocket and STOMP

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                          CLIENT LAYER                          │
├─────────────────────────────────────────────────────────────────┤
│  React + TypeScript + Redux + WebSocket + SSE + Tailwind CSS  │
└─────────────────────────┬───────────────────────────────────────┘
                          │
┌─────────────────────────┴───────────────────────────────────────┐
│                       REVERSE PROXY                            │
├─────────────────────────────────────────────────────────────────┤
│  Nginx (Port 80/443)                                          │
│  • SSL termination & Load balancing                           │
│  • Static file serving & Compression                          │
│  • Request routing & Security headers                         │
└─────────────────────────┬───────────────────────────────────────┘
                          │
┌─────────────────────────┴───────────────────────────────────────┐
│                       API GATEWAY                              │
├─────────────────────────────────────────────────────────────────┤
│  Spring Cloud Gateway (Port 8081)                             │
│  • Request routing & Load balancing                           │
│  • CORS handling & Rate limiting                              │
│  • SSE endpoint management                                    │
└─────────────────────────┬───────────────────────────────────────┘
                          │
┌─────────────────────────┴───────────────────────────────────────┐
│                   MICROSERVICES LAYER                          │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐   │
│  │   AuthService   │ │   UserService   │ │   ChatService   │   │
│  │   (Port 8082)   │ │   (Port 8084)   │ │   (Port 8085)   │   │
│  │   gRPC: 9092    │ │   gRPC: 9094    │ │   SSE Support   │   │
│  └─────────────────┘ └─────────────────┘ └─────────────────┘   │
│                                                                 │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐   │
│  │NotificationSvc  │ │ PaymentService  │ │ EurekaServer    │   │
│  │   (Port 8086)   │ │   (Port 8087)   │ │   (Port 8761)   │   │
│  │   gRPC: 9096    │ │                 │ │                 │   │
│  │   SSE Support   │ │                 │ │                 │   │
│  └─────────────────┘ └─────────────────┘ └─────────────────┘   │
└─────────────────────────┬───────────────────────────────────────┘
                          │
┌─────────────────────────┴───────────────────────────────────────┐
│                    MESSAGE STREAMING                           │
├─────────────────────────────────────────────────────────────────┤
│  Apache Kafka (Port 9092)                                     │
│  • Event streaming & Message queuing                          │
│  • Real-time data processing                                  │
│  • Microservice communication                                 │
└─────────────────────────┬───────────────────────────────────────┘
                          │
┌─────────────────────────┴───────────────────────────────────────┐
│                      DATA LAYER                                │
├─────────────────────────────────────────────────────────────────┤
│  PostgreSQL Database (Port 5432)     Redis Cache (Port 6379)  │
│  • User data & authentication        • Session management     │
│  • Chat messages & conversations     • Real-time caching      │
│  • Contact relationships             • Pub/sub messaging      │
└─────────────────────────────────────────────────────────────────┘
```

## 🛠️ Technology Stack

### **Frontend**
- **React 18.3.1** - Modern UI library with hooks and context
- **TypeScript 5.8.3** - Type-safe JavaScript development
- **Redux Toolkit** - State management with RTK Query
- **Tailwind CSS** - Utility-first CSS framework
- **Framer Motion** - Animation library for React
- **WebSocket + STOMP** - Real-time bi-directional communication
- **Server-Sent Events (SSE)** - Real-time server-to-client streaming
- **Vite** - Fast build tool and development server

### **Backend**
- **Spring Boot 3.5.0** - Java-based microservices framework
- **Spring Cloud Gateway** - API Gateway and routing
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Database abstraction layer
- **gRPC** - High-performance RPC framework
- **WebSocket + STOMP** - Real-time messaging
- **Server-Sent Events (SSE)** - Real-time event streaming
- **Eureka** - Service discovery and registration

### **Message Streaming & Event Processing**
- **Apache Kafka** - Distributed event streaming platform
- **Kafka Connect** - Data integration framework
- **Kafka Streams** - Stream processing library
- **Schema Registry** - Schema management and evolution

### **Infrastructure & Deployment**
- **Nginx** - Reverse proxy and load balancer
- **Docker** - Containerization
- **Docker Compose** - Multi-container orchestration
- **Maven** - Build automation and dependency management
- **Vercel** - Frontend deployment
- **CI/CD** - Automated testing and deployment

### **Database & Storage**
- **PostgreSQL** - Primary relational database
- **Redis** - Session storage, caching, and pub/sub messaging
- **File Storage** - Media and file uploads

## 🗃️ Redis Integration

Redis serves as a critical component in the Chatty platform, providing high-performance caching and session management capabilities:

### **Primary Use Cases**
- **Session Management**: User authentication sessions and JWT token storage
- **Caching Layer**: User profiles, chat metadata, and frequently accessed data
- **Real-time Features**: WebSocket session management and user presence tracking
- **Message Queuing**: Temporary storage for offline message delivery

### **Service Integration**
- **UserService**: User session persistence and profile caching
- **AllServices**: Centralized Redis configuration and shared caching

### **Configuration**
```properties
# Redis Configuration
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.database=0
spring.redis.timeout=2000ms
spring.redis.lettuce.pool.max-active=8
spring.redis.lettuce.pool.max-idle=8
spring.redis.lettuce.pool.min-idle=0
```

### **Benefits**
- **Performance**: Sub-millisecond response times for cached data
- **Scalability**: Distributed caching across multiple service instances
- **Reliability**: Session persistence across service restarts
- **Real-time**: Fast pub/sub messaging for live features

## 🚀 Apache Kafka Integration

Apache Kafka serves as the backbone for event-driven architecture and real-time data streaming in the Chatty platform:

### **Primary Use Cases**
- **Event Streaming**: Real-time message delivery and event propagation
- **Microservice Communication**: Asynchronous service-to-service messaging
- **Data Processing**: Stream processing for analytics and monitoring
- **Event Sourcing**: Audit trails and event history tracking

### **Topics and Partitions**
- **chat-messages**: Real-time chat message streaming
- **user-events**: User activity and presence updates
- **notifications**: Push notification delivery
- **system-events**: System-wide event logging and monitoring

### **Producer Services**
- **ChatService**: Publishes chat messages and conversation events
- **UserService**: Publishes user activity and profile updates
- **NotificationService**: Publishes notification events
- **AuthService**: Publishes authentication and security events

### **Consumer Services**
- **NotificationService**: Consumes events to trigger notifications
- **ChatService**: Consumes events for message ordering and delivery
- **Analytics Service**: Consumes events for real-time analytics
- **Audit Service**: Consumes events for compliance and logging

### **Configuration**
```properties
# Kafka Configuration
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=chatty-service-group
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer
```

### **Benefits**
- **Scalability**: Horizontal scaling with partitioned topics
- **Reliability**: Message durability and fault tolerance
- **Performance**: High-throughput, low-latency message processing
- **Decoupling**: Loose coupling between microservices

## 🌐 Nginx Configuration

Nginx acts as a reverse proxy, load balancer, and SSL terminator for the Chatty platform:

### **Primary Functions**
- **SSL Termination**: HTTPS encryption and certificate management
- **Load Balancing**: Traffic distribution across multiple service instances
- **Static File Serving**: Efficient serving of frontend assets
- **Request Routing**: Path-based routing to appropriate services
- **Security Headers**: Implementation of security best practices

### **Configuration Features**
- **Gzip Compression**: Reduces bandwidth usage and improves performance
- **Rate Limiting**: Prevents abuse and ensures fair resource usage
- **Caching**: Static content caching for improved response times
- **WebSocket Proxying**: Support for real-time WebSocket connections
- **SSE Support**: Server-Sent Events proxying for real-time updates

### **Sample Configuration**
```nginx
server {
    listen 80;
    listen 443 ssl http2;
    server_name api.chatty.com;
    
    # SSL Configuration
    ssl_certificate /etc/nginx/ssl/chatty.crt;
    ssl_certificate_key /etc/nginx/ssl/chatty.key;
    
    # Gzip Configuration
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_types text/plain application/json application/javascript text/css;
    
    # API Gateway Proxy
    location /api/ {
        proxy_pass http://api-gateway:8081/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    # WebSocket Proxy
    location /ws/ {
        proxy_pass http://api-gateway:8081/ws/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
    }
    
    # SSE Proxy
    location /sse/ {
        proxy_pass http://api-gateway:8081/sse/;
        proxy_set_header Cache-Control no-cache;
        proxy_set_header Connection '';
        proxy_http_version 1.1;
        chunked_transfer_encoding off;
        proxy_buffering off;
        proxy_cache off;
    }
}
```

### **Benefits**
- **Performance**: Efficient static file serving and caching
- **Security**: SSL termination and security header management
- **Scalability**: Load balancing and connection pooling
- **Reliability**: Health checks and failover support

## 📡 Server-Sent Events (SSE)

Server-Sent Events provide real-time, unidirectional communication from server to client:

### **Use Cases**
- **Live Notifications**: Real-time push notifications without polling
- **Chat Updates**: Live message delivery and typing indicators
- **User Presence**: Real-time online/offline status updates
- **System Alerts**: Server-side event notifications to clients

### **Implementation**
- **Spring Boot SSE**: Built-in SSE support with SseEmitter
- **Event Streaming**: Continuous event stream to connected clients
- **Auto-Reconnection**: Client-side automatic reconnection handling
- **Event Filtering**: Selective event delivery based on user context

### **Backend Implementation**
```java
@RestController
public class SSEController {
    
    @GetMapping(value = "/sse/notifications", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeToNotifications(@RequestParam String userId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        
        // Register emitter for user
        sseService.addEmitter(userId, emitter);
        
        // Handle connection cleanup
        emitter.onCompletion(() -> sseService.removeEmitter(userId, emitter));
        emitter.onTimeout(() -> sseService.removeEmitter(userId, emitter));
        
        return emitter;
    }
    
    @Service
    public class SSEService {
        private final Map<String, List<SseEmitter>> userEmitters = new ConcurrentHashMap<>();
        
        public void sendNotification(String userId, Object data) {
            List<SseEmitter> emitters = userEmitters.get(userId);
            if (emitters != null) {
                emitters.forEach(emitter -> {
                    try {
                        emitter.send(SseEmitter.event()
                            .name("notification")
                            .data(data)
                            .id(UUID.randomUUID().toString()));
                    } catch (Exception e) {
                        emitter.completeWithError(e);
                    }
                });
            }
        }
    }
}
```

### **Frontend Implementation**
```typescript
// SSE Client Service
export class SSEService {
    private eventSource: EventSource | null = null;
    
    connect(userId: string) {
        this.eventSource = new EventSource(`/sse/notifications?userId=${userId}`);
        
        this.eventSource.onmessage = (event) => {
            const data = JSON.parse(event.data);
            this.handleNotification(data);
        };
        
        this.eventSource.onerror = (error) => {
            console.error('SSE connection error:', error);
            this.reconnect(userId);
        };
    }
    
    private reconnect(userId: string) {
        setTimeout(() => {
            this.connect(userId);
        }, 5000);
    }
    
    private handleNotification(data: any) {
        // Dispatch to Redux store or handle notification
        store.dispatch(addNotification(data));
    }
}
```

### **Benefits**
- **Real-time**: Instant server-to-client communication
- **Efficiency**: Lower overhead compared to polling
- **Simplicity**: Built-in browser support and automatic reconnection
- **Scalability**: Efficient handling of many concurrent connections

## 🔧 Microservices

### **1. Auth Service** (Port 8082, gRPC: 9092)
- User authentication and authorization
- JWT token generation and validation
- Password hashing and security
- Session management

### **2. User Service** (Port 8084, gRPC: 9094)
- User profile management
- User search and discovery
- Profile picture and settings
- User data persistence

### **3. Chat Service** (Port 8085)
- Message handling and storage
- Real-time message delivery
- File upload and sharing
- Message history and pagination

### **4. Notification Service** (Port 8086, gRPC: 9096)
- Contact request management
- Real-time notifications
- Push notification system
- Notification preferences

### **5. Payment Service** (Port 8087)
- Payment processing
- Transaction history
- Billing and subscriptions
- Payment gateway integration

### **6. API Gateway** (Port 8081)
- Request routing and load balancing
- CORS and security headers
- Rate limiting and throttling
- Request/response transformation

### **7. Eureka Server** (Port 8761)
- Service discovery and registration
- Health monitoring
- Load balancing configuration
- Service mesh coordination

## 🚀 Getting Started

### **Prerequisites**
- **Java 21** or later
- **Node.js 18** or later
- **PostgreSQL 14** or later
- **Redis 6.0** or later
- **Maven 3.8** or later
- **Git**

### **Backend Setup**

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-username/chatty-microservices.git
   cd chatty-microservices
   ```

2. **Setup PostgreSQL Database**
   ```sql
   CREATE DATABASE chatty_microservices;
   CREATE USER chatty_user WITH ENCRYPTED PASSWORD 'your_password';
   GRANT ALL PRIVILEGES ON DATABASE chatty_microservices TO chatty_user;
   ```

3. **Setup Redis**
   ```bash
   # Install Redis (Ubuntu/Debian)
   sudo apt update
   sudo apt install redis-server
   
   # Start Redis server
   sudo systemctl start redis-server
   sudo systemctl enable redis-server
   
   # Verify Redis is running
   redis-cli ping
   # Should return: PONG
   ```

4. **Configure Application Properties**
   ```bash
   # Update database and Redis credentials in each service's application.properties
   cd server/AuthService/src/main/resources
   # Edit application.properties with your database and Redis credentials
   ```

5. **Build and Start Services**
   ```bash
   # Build proto definitions first
   cd server/chattyprotos
   mvn clean install
   
   # Start Eureka Server
   cd ../EurekaServer
   mvn spring-boot:run
   
   # Start services in this order:
   cd ../AuthService && mvn spring-boot:run &
   cd ../UserService && mvn spring-boot:run &
   cd ../NotificationService && mvn spring-boot:run &
   cd ../ChatService && mvn spring-boot:run &
   cd ../PaymentService && mvn spring-boot:run &
   cd ../Api_Gateway && mvn spring-boot:run &
   ```

### **Frontend Setup**

1. **Install Dependencies**
   ```bash
   cd client
   npm install
   ```

2. **Configure Environment**
   ```bash
   # Create .env file
   cp .env.example .env
   # Edit .env with your API endpoints
   ```

3. **Start Development Server**
   ```bash
   npm run dev
   ```

4. **Build for Production**
   ```bash
   npm run build
   ```

### **Docker Setup (Recommended)**

```bash
# Build and run all services with full stack
docker-compose up -d

# Or build specific services
docker-compose up -d postgres redis kafka zookeeper nginx eureka-server api-gateway

# Check service status
docker-compose ps

# View logs for specific service
docker-compose logs -f kafka
docker-compose logs -f nginx
```

**Docker Compose Services:**
- **Nginx** (Port 80/443) - Reverse proxy and load balancer
- **PostgreSQL** (Port 5432) - Primary database
- **Redis** (Port 6379) - Caching and session management
- **Apache Kafka** (Port 9092) - Event streaming platform
- **Zookeeper** (Port 2181) - Kafka coordination service
- **Eureka Server** (Port 8761) - Service discovery
- **API Gateway** (Port 8081) - Request routing and management
- **All microservices** - Each with their respective ports

### **Kafka Setup**
```bash
# Create Kafka topics
docker-compose exec kafka kafka-topics --create --topic chat-messages --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
docker-compose exec kafka kafka-topics --create --topic user-events --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
docker-compose exec kafka kafka-topics --create --topic notifications --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1

# List all topics
docker-compose exec kafka kafka-topics --list --bootstrap-server localhost:9092

# Monitor topic messages
docker-compose exec kafka kafka-console-consumer --topic chat-messages --bootstrap-server localhost:9092
```

### **Nginx Configuration**
```bash
# Reload Nginx configuration
docker-compose exec nginx nginx -s reload

# Test Nginx configuration
docker-compose exec nginx nginx -t

# View Nginx access logs
docker-compose logs -f nginx
```

## 📚 API Documentation

### **Authentication Endpoints**
```http
POST /auth/login
POST /auth/register
POST /auth/validate
GET  /auth/ping
```

### **User Management**
```http
GET    /user/profile
PUT    /user/profile
GET    /user/search?q={query}
POST   /user/upload-avatar
```

### **Chat Endpoints**
```http
GET    /chat/allChats
POST   /chat/send
GET    /chat/messages/{chatId}
POST   /files/upload
```

### **Contact Management**
```http
GET    /contacts
POST   /contacts/request
PUT    /contacts/accept/{id}
DELETE /contacts/reject/{id}
```

### **gRPC Services**

#### **Auth Service gRPC**
```protobuf
service AuthService {
  rpc ValidateToken (TokenRequest) returns (TokenResponse);
}
```

#### **User Service gRPC**
```protobuf
service UserService {
  rpc GetUserByEmail (GetUserByEmailRequest) returns (UserResponse);
  rpc GetAllUser (Empty) returns (AllUserResponse);
}
```

#### **Contact Service gRPC**
```protobuf
service ContactService {
  rpc GetUserContacts (GetUserContactsRequest) returns (GetUserContactsResponse);
}
```

## ⚡ Real-time Features

### **WebSocket Connections**
- **Chat Messages**: `/user/queue/messages`
- **Notifications**: `/user/queue/notifications`
- **Presence Updates**: `/topic/presence`
- **Typing Indicators**: `/topic/typing`

### **Server-Sent Events (SSE)**
- **Live Notifications**: `/sse/notifications`
- **System Alerts**: `/sse/system-events`
- **User Presence**: `/sse/presence`
- **Chat Updates**: `/sse/chat-updates`

### **Kafka Event Streaming**
- **Message Events**: Real-time message delivery via Kafka streams
- **User Activity**: Activity tracking and presence updates
- **System Events**: Distributed event processing
- **Analytics**: Real-time analytics and monitoring

### **STOMP Messaging**
```javascript
// Send private message
stompClient.send("/app/private-message", {}, JSON.stringify({
  to: "user@example.com",
  content: "Hello!",
  type: "text"
}));

// Subscribe to notifications
stompClient.subscribe("/user/queue/notifications", (message) => {
  const notification = JSON.parse(message.body);
  // Handle notification
});
```

### **SSE Implementation**
```javascript
// Connect to SSE endpoint
const eventSource = new EventSource('/sse/notifications');

eventSource.onmessage = (event) => {
  const data = JSON.parse(event.data);
  dispatch(addNotification(data));
};

eventSource.addEventListener('user-presence', (event) => {
  const presence = JSON.parse(event.data);
  dispatch(updateUserPresence(presence));
});
```

### **Kafka Integration**
```javascript
// Kafka producer example
@Component
public class MessageProducer {
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    public void sendMessage(String topic, Object message) {
        kafkaTemplate.send(topic, message);
    }
}

// Kafka consumer example
@KafkaListener(topics = "chat-messages")
public void handleChatMessage(String message) {
    // Process incoming chat message
    ChatMessage chatMessage = objectMapper.readValue(message, ChatMessage.class);
    messageService.processMessage(chatMessage);
}
```

### **Redux Integration**
- Real-time state updates via WebSocket and SSE
- Optimistic UI updates with rollback support
- Persistent state management with Redux Toolkit
- Offline support with queue synchronization

## 🔒 Security

### **Authentication**
- JWT tokens with RS256 signing
- Refresh token rotation
- Session invalidation
- Password strength validation

### **Authorization**
- Role-based access control
- API endpoint protection
- Resource-level permissions
- Cross-service authorization

### **Communication**
- HTTPS/TLS encryption
- gRPC with TLS
- WebSocket Secure (WSS)
- Request signing

### **Data Protection**
- SQL injection prevention
- XSS protection
- CORS configuration
- Rate limiting

## 🎨 UI/UX Features

### **Design System**
- Paper-inspired material design
- Consistent color palette
- Typography hierarchy
- Responsive grid system

### **Animations**
- Smooth page transitions
- Loading states
- Hover effects
- Micro-interactions

### **Accessibility**
- ARIA labels and roles
- Keyboard navigation
- Screen reader support
- Color contrast compliance

## 📊 Performance

### **Frontend Optimization**
- Code splitting and lazy loading
- Image optimization
- Bundle size optimization
- Service worker caching

### **Backend Performance**
- Database query optimization
- Connection pooling
- Caching with Redis
- Load balancing

### **Monitoring**
- Application metrics
- Error tracking
- Performance monitoring
- Health checks

## 🧪 Testing

### **Frontend Testing**
```bash
# Run unit tests
npm test

# Run E2E tests
npm run test:e2e

# Run coverage report
npm run test:coverage
```

### **Backend Testing**
```bash
# Run unit tests
mvn test

# Run integration tests
mvn verify

# Run service tests
mvn spring-boot:run -Dspring-boot.run.profiles=test
```

## 🚀 Deployment

### **Frontend Deployment (Vercel)**
```bash
# Deploy to Vercel
vercel --prod

# Or use GitHub integration
# Connect repository to Vercel dashboard
```

### **Backend Deployment (Docker)**
```bash
# Build images
docker-compose build

# Deploy to production
docker-compose -f docker-compose.prod.yml up -d
```

### **Environment Variables**
```bash
# Frontend (.env)
VITE_API_URL=https://api.chatty.com
VITE_WS_URL=wss://ws.chatty.com

# Backend (application.properties)
spring.datasource.url=jdbc:postgresql://localhost:5432/chatty
jwt.secret=your-secret-key
spring.profiles.active=production
```

## 🤝 Contributing

We welcome contributions! Please follow these steps:

1. **Fork the repository**
2. **Create a feature branch**
   ```bash
   git checkout -b feature/amazing-feature
   ```
3. **Commit your changes**
   ```bash
   git commit -m 'Add amazing feature'
   ```
4. **Push to the branch**
   ```bash
   git push origin feature/amazing-feature
   ```
5. **Open a Pull Request**

### **Development Guidelines**
- Follow conventional commits
- Add tests for new features
- Update documentation
- Ensure code quality with ESLint/Checkstyle

## 📜 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👏 Acknowledgments

- **Spring Boot Team** for the excellent framework
- **React Team** for the amazing UI library
- **Tailwind CSS** for the utility-first approach
- **gRPC Team** for high-performance RPC
- **Open Source Community** for inspiration and support

## 📞 Contact

- **Developer**: [Dev-Rishav](https://github.com/Dev-Rishav)
- **Email**: dev.rishav@example.com
- **LinkedIn**: [Connect with me](https://linkedin.com/in/dev-rishav)

---

<div align="center">
  <p>Made with ❤️ by <a href="https://github.com/Dev-Rishav">Dev-Rishav</a></p>
  <p>⭐ Star this repository if you found it helpful!</p>
</div>
