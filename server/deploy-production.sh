#!/bin/bash

# Production Build and Deployment Script for Chatty Microservices
# This script builds and deploys all services for AWS production environment

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
PROJECT_NAME="chatty-microservices"
ENVIRONMENT="prod"
VERSION="1.0.0"

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if required tools are installed
check_dependencies() {
    print_status "Checking dependencies..."
    
    # Check Docker
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed. Please install Docker first."
        exit 1
    fi
    
    # Check Docker Compose
    if ! command -v docker-compose &> /dev/null; then
        print_error "Docker Compose is not installed. Please install Docker Compose first."
        exit 1
    fi
    
    # Check if Docker is running
    if ! docker info &> /dev/null; then
        print_error "Docker is not running. Please start Docker first."
        exit 1
    fi
    
    print_success "All dependencies are available"
}

# Function to clean up previous builds
cleanup() {
    print_status "Cleaning up previous builds..."
    
    # Stop and remove containers
    docker-compose -f docker-compose.yml down --remove-orphans || true
    
    # Remove dangling images
    docker image prune -f || true
    
    # Remove unused volumes (optional - comment out if you want to keep data)
    # docker volume prune -f || true
    
    print_success "Cleanup completed"
}

# Function to build proto definitions
build_protos() {
    print_status "Building proto definitions..."
    
    if [ -d "chattyprotos" ]; then
        cd chattyprotos
        mvn clean install -DskipTests
        cd ..
        print_success "Proto definitions built successfully"
    else
        print_warning "No proto definitions found, skipping..."
    fi
}

# Function to build individual service
build_service() {
    local service_name=$1
    local service_dir=$2
    
    print_status "Building $service_name..."
    
    if [ -d "$service_dir" ]; then
        cd "$service_dir"
        
        # Build with Maven
        print_status "Running Maven build for $service_name..."
        mvn clean package -DskipTests -Dspring.profiles.active=prod
        
        # Build Docker image
        print_status "Building Docker image for $service_name..."
        docker build -t "$PROJECT_NAME-$service_name:$VERSION" .
        docker tag "$PROJECT_NAME-$service_name:$VERSION" "$PROJECT_NAME-$service_name:latest"
        
        cd ..
        print_success "$service_name built successfully"
    else
        print_warning "$service_name directory not found, skipping..."
    fi
}

# Function to build all services
build_all_services() {
    print_status "Building all microservices..."
    
    # Build services in dependency order
    build_service "eureka-server" "EurekaServer"
    build_service "auth-service" "AuthService"
    build_service "user-service" "userservice"
    build_service "chat-service" "chatservice"
    build_service "notification-service" "notificationservice"
    build_service "payment-service" "PaymentService"
    build_service "api-gateway" "Api_Gateway"
    
    print_success "All services built successfully"
}

# Function to create environment file
create_env_file() {
    print_status "Creating production environment file..."
    
    if [ ! -f ".env" ]; then
        cp .env.example .env
        print_warning "Created .env file from example. Please update with your production values!"
        print_warning "Edit .env file with your actual production values before deploying."
    else
        print_success ".env file already exists"
    fi
}

# Function to start services
start_services() {
    print_status "Starting all services..."
    
    # Start infrastructure services first
    print_status "Starting infrastructure services (PostgreSQL, Redis, Kafka)..."
    docker-compose up -d postgres redis zookeeper kafka
    
    # Wait for infrastructure to be ready
    print_status "Waiting for infrastructure services to be ready..."
    sleep 30
    
    # Start Eureka server
    print_status "Starting Eureka server..."
    docker-compose up -d eureka-server
    
    # Wait for Eureka to be ready
    print_status "Waiting for Eureka server to be ready..."
    sleep 30
    
    # Start application services
    print_status "Starting application services..."
    docker-compose up -d auth-service user-service chat-service notification-service payment-service
    
    # Wait for services to register
    print_status "Waiting for services to register with Eureka..."
    sleep 30
    
    # Start API Gateway last
    print_status "Starting API Gateway..."
    docker-compose up -d api-gateway
    
    print_success "All services started successfully"
}

# Function to check service health
check_health() {
    print_status "Checking service health..."
    
    services=(
        "eureka-server:8761"
        "auth-service:8080"
        "user-service:8082"
        "chat-service:8081"
        "notification-service:8083"
        "payment-service:8084"
        "api-gateway:8085"
    )
    
    for service in "${services[@]}"; do
        IFS=':' read -ra ADDR <<< "$service"
        service_name="${ADDR[0]}"
        port="${ADDR[1]}"
        
        print_status "Checking $service_name health..."
        
        # Wait for service to be ready
        for i in {1..10}; do
            if curl -f "http://localhost:$port/actuator/health" &> /dev/null; then
                print_success "$service_name is healthy"
                break
            else
                if [ $i -eq 10 ]; then
                    print_error "$service_name is not responding"
                else
                    print_status "Waiting for $service_name... (attempt $i/10)"
                    sleep 10
                fi
            fi
        done
    done
}

# Function to show deployment summary
show_summary() {
    print_success "🚀 Deployment Summary 🚀"
    echo ""
    echo "Services are now running at:"
    echo "  • Eureka Server: http://localhost:8761"
    echo "  • API Gateway: http://localhost:8085"
    echo "  • Auth Service: http://localhost:8080"
    echo "  • User Service: http://localhost:8082"
    echo "  • Chat Service: http://localhost:8081"
    echo "  • Notification Service: http://localhost:8083"
    echo "  • Payment Service: http://localhost:8084"
    echo ""
    echo "Infrastructure:"
    echo "  • PostgreSQL: localhost:5432"
    echo "  • Redis: localhost:6379"
    echo "  • Kafka: localhost:9092"
    echo ""
    echo "Monitoring:"
    echo "  • Docker: docker-compose ps"
    echo "  • Logs: docker-compose logs -f [service-name]"
    echo "  • Health: curl http://localhost:8085/actuator/health"
    echo ""
    print_success "Deployment completed successfully! 🎉"
}

# Function to show usage
show_usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  --build-only    Only build services, don't start them"
    echo "  --start-only    Only start services (assumes they're already built)"
    echo "  --cleanup       Clean up previous builds and containers"
    echo "  --health-check  Check health of running services"
    echo "  --help          Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0                 # Full build and deployment"
    echo "  $0 --build-only    # Build all services"
    echo "  $0 --start-only    # Start services"
    echo "  $0 --cleanup       # Clean up everything"
}

# Main execution
main() {
    case "${1:-}" in
        --build-only)
            check_dependencies
            build_protos
            build_all_services
            ;;
        --start-only)
            check_dependencies
            create_env_file
            start_services
            check_health
            show_summary
            ;;
        --cleanup)
            cleanup
            ;;
        --health-check)
            check_health
            ;;
        --help)
            show_usage
            ;;
        "")
            # Full deployment
            check_dependencies
            cleanup
            build_protos
            build_all_services
            create_env_file
            start_services
            check_health
            show_summary
            ;;
        *)
            print_error "Unknown option: $1"
            show_usage
            exit 1
            ;;
    esac
}

# Run main function with all arguments
main "$@"
