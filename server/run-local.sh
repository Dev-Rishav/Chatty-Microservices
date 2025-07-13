#!/bin/bash

# Local Docker Development Script
# This script helps you run the entire backend locally with Docker

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if Docker is running
check_docker() {
    if ! docker info > /dev/null 2>&1; then
        print_error "Docker is not running. Please start Docker first."
        exit 1
    fi
    print_status "Docker is running ✓"
}

# Function to check if Docker Compose is available
check_docker_compose() {
    if ! command -v docker > /dev/null 2>&1; then
        print_error "Docker is not installed. Please install Docker first."
        exit 1
    fi
    
    # Check if docker compose (newer version) is available
    if docker compose version > /dev/null 2>&1; then
        COMPOSE_CMD="docker compose"
    elif command -v docker-compose > /dev/null 2>&1; then
        COMPOSE_CMD="docker-compose"
    else
        print_error "Docker Compose is not available. Please install Docker Compose."
        exit 1
    fi
    
    print_status "Docker Compose is available ✓"
}

# Function to build and start services
start_services() {
    print_status "Building and starting all services..."
    
    # Build proto definitions first
    print_status "Building proto definitions..."
    cd chattyprotos
    if command -v mvn > /dev/null 2>&1; then
        mvn clean install -DskipTests
    else
        print_warning "Maven not found. Using Docker to build proto definitions..."
        docker run --rm -v "$(pwd)":/usr/src/app -w /usr/src/app maven:3.9.5-eclipse-temurin-21 mvn clean install -DskipTests
    fi
    cd ..
    
    # Start services with docker-compose
    print_status "Starting services with Docker Compose..."
    $COMPOSE_CMD -f docker-compose.local.yml up --build -d
    
    print_status "Services are starting... This may take a few minutes."
    print_status "You can monitor the logs with: $COMPOSE_CMD -f docker-compose.local.yml logs -f"
}

# Function to stop services
stop_services() {
    print_status "Stopping all services..."
    $COMPOSE_CMD -f docker-compose.local.yml down
    print_status "All services stopped."
}

# Function to restart services
restart_services() {
    print_status "Restarting all services..."
    $COMPOSE_CMD -f docker-compose.local.yml down
    $COMPOSE_CMD -f docker-compose.local.yml up --build -d
    print_status "All services restarted."
}

# Function to show logs
show_logs() {
    SERVICE=$1
    if [ -z "$SERVICE" ]; then
        print_status "Showing logs for all services..."
        $COMPOSE_CMD -f docker-compose.local.yml logs -f
    else
        print_status "Showing logs for $SERVICE..."
        $COMPOSE_CMD -f docker-compose.local.yml logs -f $SERVICE
    fi
}

# Function to check service status
check_status() {
    check_docker_compose
    print_status "Checking service status..."
    $COMPOSE_CMD -f docker-compose.local.yml ps
    
    echo ""
    print_status "Service Health Status:"
    
    # Check each service health
    services=("postgres" "redis" "eureka-server" "auth-service" "user-service" "chat-service" "notification-service" "payment-service" "api-gateway")
    
    for service in "${services[@]}"; do
        if $COMPOSE_CMD -f docker-compose.local.yml ps -q $service > /dev/null 2>&1; then
            status=$($COMPOSE_CMD -f docker-compose.local.yml ps $service | grep $service | awk '{print $4}')
            if [[ $status == *"Up"* ]]; then
                echo -e "  ${GREEN}✓${NC} $service: Running"
            else
                echo -e "  ${RED}✗${NC} $service: $status"
            fi
        else
            echo -e "  ${RED}✗${NC} $service: Not found"
        fi
    done
}

# Function to run database migrations
run_migrations() {
    print_status "Running database migrations..."
    
    # Wait for postgres to be ready
    print_status "Waiting for PostgreSQL to be ready..."
    until docker-compose -f docker-compose.local.yml exec postgres pg_isready -U postgres > /dev/null 2>&1; do
        echo -n "."
        sleep 2
    done
    echo ""
    
    print_status "PostgreSQL is ready. Database will be initialized automatically."
}

# Function to clean up (remove containers and volumes)
cleanup() {
    print_warning "This will remove all containers and volumes. Are you sure? (y/N)"
    read -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        print_status "Cleaning up containers and volumes..."
        docker-compose -f docker-compose.local.yml down -v --remove-orphans
        docker system prune -f
        print_status "Cleanup completed."
    else
        print_status "Cleanup cancelled."
    fi
}

# Function to show help
show_help() {
    echo "Chatty Microservices Local Development Script"
    echo ""
    echo "Usage: $0 [COMMAND] [OPTIONS]"
    echo ""
    echo "Commands:"
    echo "  start                    Start all services"
    echo "  stop                     Stop all services"
    echo "  restart                  Restart all services"
    echo "  status                   Show service status"
    echo "  logs [service]           Show logs (all services if no service specified)"
    echo "  migrate                  Run database migrations"
    echo "  cleanup                  Remove all containers and volumes"
    echo "  help                     Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 start                 # Start all services"
    echo "  $0 logs auth-service     # Show logs for auth service"
    echo "  $0 status                # Check service status"
    echo ""
    echo "Service URLs (once started):"
    echo "  - API Gateway:     http://localhost:8081"
    echo "  - Auth Service:    http://localhost:8082"
    echo "  - User Service:    http://localhost:8084"
    echo "  - Chat Service:    http://localhost:8085"
    echo "  - Notification:    http://localhost:8086"
    echo "  - Payment Service: http://localhost:8087"
    echo "  - Eureka Server:   http://localhost:8761"
    echo "  - PostgreSQL:      localhost:5432"
    echo "  - Redis:           localhost:6379"
}

# Main script logic
case $1 in
    start)
        check_docker
        check_docker_compose
        start_services
        ;;
    stop)
        stop_services
        ;;
    restart)
        check_docker
        check_docker_compose
        restart_services
        ;;
    status)
        check_status
        ;;
    logs)
        show_logs $2
        ;;
    migrate)
        run_migrations
        ;;
    cleanup)
        cleanup
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        print_error "Unknown command: $1"
        echo ""
        show_help
        exit 1
        ;;
esac
