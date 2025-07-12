# 💬 Chatty Microservices

<div align="center">
  <img src="https://img.shields.io/badge/Spring_Boot-3.5.0-6DB33F?style=for-the-badge&logo=spring&logoColor=white" alt="Spring Boot">
  <img src="https://img.shields.io/badge/React-18.3.1-61DAFB?style=for-the-badge&logo=react&logoColor=white" alt="React">
  <img src="https://img.shields.io/badge/TypeScript-5.8.3-3178C6?style=for-the-badge&logo=typescript&logoColor=white" alt="TypeScript">
  <img src="https://img.shields.io/badge/PostgreSQL-336791?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL">
  <img src="https://img.shields.io/badge/gRPC-4285F4?style=for-the-badge&logo=google&logoColor=white" alt="gRPC">
  <img src="https://img.shields.io/badge/WebSocket-010101?style=for-the-badge&logo=websocket&logoColor=white" alt="WebSocket">
</div>

<div align="center">
  <h3>🚀 A modern, scalable real-time chat application built with microservices architecture</h3>
  <p>Experience seamless messaging with enterprise-grade architecture, real-time communication, and beautiful UI</p>
</div>

## 🌟 Live Demo

**🎯 [chatty-theta-five.vercel.app](https://chatty-theta-five.vercel.app)**

## 📖 Table of Contents

- [Features](#-features)
- [Architecture](#-architecture)
- [Technology Stack](#-technology-stack)
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
│  React + TypeScript + Redux + WebSocket + Tailwind CSS        │
└─────────────────────────┬───────────────────────────────────────┘
                          │
┌─────────────────────────┴───────────────────────────────────────┐
│                       API GATEWAY                              │
├─────────────────────────────────────────────────────────────────┤
│  Spring Cloud Gateway (Port 8081)                             │
│  • Request routing & Load balancing                           │
│  • CORS handling & Rate limiting                              │
└─────────────────────────┬───────────────────────────────────────┘
                          │
┌─────────────────────────┴───────────────────────────────────────┐
│                   MICROSERVICES LAYER                          │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐   │
│  │   AuthService   │ │   UserService   │ │   ChatService   │   │
│  │   (Port 8082)   │ │   (Port 8084)   │ │   (Port 8085)   │   │
│  │   gRPC: 9092    │ │   gRPC: 9094    │ │                 │   │
│  └─────────────────┘ └─────────────────┘ └─────────────────┘   │
│                                                                 │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐   │
│  │NotificationSvc  │ │ PaymentService  │ │ EurekaServer    │   │
│  │   (Port 8086)   │ │   (Port 8087)   │ │   (Port 8761)   │   │
│  │   gRPC: 9096    │ │                 │ │                 │   │
│  └─────────────────┘ └─────────────────┘ └─────────────────┘   │
└─────────────────────────┬───────────────────────────────────────┘
                          │
┌─────────────────────────┴───────────────────────────────────────┐
│                      DATA LAYER                                │
├─────────────────────────────────────────────────────────────────┤
│  PostgreSQL Database (Port 5432)                              │
│  • User data & authentication                                 │
│  • Chat messages & conversations                              │
│  • Contact relationships                                      │
└─────────────────────────────────────────────────────────────────┘
```

## 🛠️ Technology Stack

### **Frontend**
- **React 18.3.1** - Modern UI library with hooks and context
- **TypeScript 5.8.3** - Type-safe JavaScript development
- **Redux Toolkit** - State management with RTK Query
- **Tailwind CSS** - Utility-first CSS framework
- **Framer Motion** - Animation library for React
- **WebSocket + STOMP** - Real-time communication
- **Vite** - Fast build tool and development server

### **Backend**
- **Spring Boot 3.5.0** - Java-based microservices framework
- **Spring Cloud Gateway** - API Gateway and routing
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Database abstraction layer
- **gRPC** - High-performance RPC framework
- **WebSocket + STOMP** - Real-time messaging
- **Eureka** - Service discovery and registration

### **Database & Storage**
- **PostgreSQL** - Primary relational database
- **Redis** - Session storage, caching, and pub/sub messaging
- **File Storage** - Media and file uploads

### **DevOps & Deployment**
- **Docker** - Containerization
- **Maven** - Build automation and dependency management
- **Vercel** - Frontend deployment
- **CI/CD** - Automated testing and deployment

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

### **Docker Setup (Optional)**

```bash
# Build and run all services with Redis
docker-compose up -d

# Or build specific services
docker-compose up -d postgres redis eureka-server api-gateway

# Check Redis container status
docker-compose ps redis
```

**Docker Compose Services:**
- PostgreSQL (Port 5432)
- Redis (Port 6379)
- Eureka Server (Port 8761)
- API Gateway (Port 8081)
- All microservices with their respective ports

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

### **Redux Integration**
- Real-time state updates
- Optimistic UI updates
- Persistent state management
- Offline support

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
