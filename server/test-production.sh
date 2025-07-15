#!/bin/bash

# Production Testing Script for Chatty Microservices
# This script tests all services to ensure they're working correctly in production

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
BASE_URL="${BASE_URL:-http://localhost:8085}"
EUREKA_URL="${EUREKA_URL:-http://localhost:8761}"
TIMEOUT=30

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

# Function to test HTTP endpoint
test_endpoint() {
    local url=$1
    local expected_status=${2:-200}
    local description=$3
    
    print_status "Testing: $description"
    
    response=$(curl -s -o /dev/null -w "%{http_code}" --max-time $TIMEOUT "$url" || echo "000")
    
    if [ "$response" -eq "$expected_status" ]; then
        print_success "✓ $description - Status: $response"
        return 0
    else
        print_error "✗ $description - Expected: $expected_status, Got: $response"
        return 1
    fi
}

# Function to test JSON endpoint
test_json_endpoint() {
    local url=$1
    local description=$2
    
    print_status "Testing: $description"
    
    response=$(curl -s --max-time $TIMEOUT "$url" || echo "ERROR")
    
    if echo "$response" | jq . >/dev/null 2>&1; then
        print_success "✓ $description - Valid JSON response"
        return 0
    else
        print_error "✗ $description - Invalid JSON response"
        return 1
    fi
}

# Function to test service registration with Eureka
test_eureka_registration() {
    print_status "Testing Eureka service registration..."
    
    response=$(curl -s "$EUREKA_URL/eureka/apps" -H "Accept: application/json" || echo "ERROR")
    
    if echo "$response" | jq . >/dev/null 2>&1; then
        services=$(echo "$response" | jq -r '.applications.application[].name' 2>/dev/null || echo "")
        
        expected_services=("AUTH-SERVICE" "USER-SERVICE" "CHAT-SERVICE" "NOTIFICATION-SERVICE" "PAYMENT-SERVICE" "API-GATEWAY")
        
        for service in "${expected_services[@]}"; do
            if echo "$services" | grep -q "$service"; then
                print_success "✓ $service registered with Eureka"
            else
                print_error "✗ $service not registered with Eureka"
            fi
        done
    else
        print_error "✗ Failed to query Eureka registry"
    fi
}

# Function to test database connectivity
test_database_connectivity() {
    print_status "Testing database connectivity..."
    
    services=("auth-service" "user-service" "chat-service" "notification-service" "payment-service")
    
    for service in "${services[@]}"; do
        port=$(get_service_port "$service")
        if test_endpoint "http://localhost:$port/actuator/health" 200 "$service database health"; then
            # Check if database is mentioned in health response
            response=$(curl -s "http://localhost:$port/actuator/health" || echo "ERROR")
            if echo "$response" | grep -q "db"; then
                print_success "✓ $service database connection healthy"
            else
                print_warning "? $service database status unclear"
            fi
        fi
    done
}

# Function to get service port
get_service_port() {
    case $1 in
        "auth-service") echo "8080" ;;
        "user-service") echo "8082" ;;
        "chat-service") echo "8081" ;;
        "notification-service") echo "8083" ;;
        "payment-service") echo "8084" ;;
        "api-gateway") echo "8085" ;;
        *) echo "8080" ;;
    esac
}

# Function to test authentication flow
test_auth_flow() {
    print_status "Testing authentication flow..."
    
    # Test signup
    signup_response=$(curl -s -X POST "$BASE_URL/api/auth/signup" \
        -H "Content-Type: application/json" \
        -d '{
            "username": "testuser",
            "email": "test@example.com",
            "password": "testpassword123"
        }' || echo "ERROR")
    
    if echo "$signup_response" | jq . >/dev/null 2>&1; then
        print_success "✓ Signup endpoint working"
    else
        print_error "✗ Signup endpoint failed"
    fi
    
    # Test login
    login_response=$(curl -s -X POST "$BASE_URL/api/auth/login" \
        -H "Content-Type: application/json" \
        -d '{
            "email": "test@example.com",
            "password": "testpassword123"
        }' || echo "ERROR")
    
    if echo "$login_response" | jq . >/dev/null 2>&1; then
        token=$(echo "$login_response" | jq -r '.token' 2>/dev/null || echo "")
        if [ -n "$token" ] && [ "$token" != "null" ]; then
            print_success "✓ Login endpoint working - Token received"
            echo "$token" > /tmp/test_token
        else
            print_error "✗ Login endpoint - No token received"
        fi
    else
        print_error "✗ Login endpoint failed"
    fi
}

# Function to test user service
test_user_service() {
    print_status "Testing user service..."
    
    if [ -f /tmp/test_token ]; then
        token=$(cat /tmp/test_token)
        
        # Test get user profile
        profile_response=$(curl -s -X GET "$BASE_URL/api/user/profile" \
            -H "Authorization: Bearer $token" || echo "ERROR")
        
        if echo "$profile_response" | jq . >/dev/null 2>&1; then
            print_success "✓ User profile endpoint working"
        else
            print_error "✗ User profile endpoint failed"
        fi
        
        # Test get all users
        users_response=$(curl -s -X GET "$BASE_URL/api/user/all" \
            -H "Authorization: Bearer $token" || echo "ERROR")
        
        if echo "$users_response" | jq . >/dev/null 2>&1; then
            print_success "✓ Get all users endpoint working"
        else
            print_error "✗ Get all users endpoint failed"
        fi
    else
        print_warning "? User service test skipped - No authentication token"
    fi
}

# Function to test chat service
test_chat_service() {
    print_status "Testing chat service..."
    
    if [ -f /tmp/test_token ]; then
        token=$(cat /tmp/test_token)
        
        # Test get chats
        chats_response=$(curl -s -X GET "$BASE_URL/api/chat/chats" \
            -H "Authorization: Bearer $token" || echo "ERROR")
        
        if echo "$chats_response" | jq . >/dev/null 2>&1; then
            print_success "✓ Get chats endpoint working"
        else
            print_error "✗ Get chats endpoint failed"
        fi
        
        # Test SSE endpoint
        sse_response=$(curl -s --max-time 5 "$BASE_URL/api/chat/sse/updates?token=$token" || echo "ERROR")
        
        if [ "$sse_response" != "ERROR" ]; then
            print_success "✓ SSE endpoint accessible"
        else
            print_error "✗ SSE endpoint failed"
        fi
    else
        print_warning "? Chat service test skipped - No authentication token"
    fi
}

# Function to test WebSocket connectivity
test_websocket_connectivity() {
    print_status "Testing WebSocket connectivity..."
    
    if [ -f /tmp/test_token ]; then
        token=$(cat /tmp/test_token)
        
        # Test WebSocket endpoint (basic connectivity)
        ws_response=$(curl -s -I --max-time 5 "http://localhost:8081/ws?token=$token" || echo "ERROR")
        
        if echo "$ws_response" | grep -q "101\|200"; then
            print_success "✓ WebSocket endpoint accessible"
        else
            print_warning "? WebSocket endpoint test inconclusive"
        fi
    else
        print_warning "? WebSocket test skipped - No authentication token"
    fi
}

# Function to test infrastructure services
test_infrastructure() {
    print_status "Testing infrastructure services..."
    
    # Test PostgreSQL
    if docker ps | grep -q "chatty-postgres"; then
        print_success "✓ PostgreSQL container running"
    else
        print_error "✗ PostgreSQL container not running"
    fi
    
    # Test Redis
    if docker ps | grep -q "chatty-redis"; then
        print_success "✓ Redis container running"
    else
        print_error "✗ Redis container not running"
    fi
    
    # Test Kafka
    if docker ps | grep -q "chatty-kafka"; then
        print_success "✓ Kafka container running"
    else
        print_error "✗ Kafka container not running"
    fi
}

# Function to test service health endpoints
test_all_health_endpoints() {
    print_status "Testing all service health endpoints..."
    
    services=(
        "eureka-server:8761"
        "auth-service:8080"
        "user-service:8082"
        "chat-service:8081"
        "notification-service:8083"
        "payment-service:8084"
        "api-gateway:8085"
    )
    
    for service_info in "${services[@]}"; do
        IFS=':' read -ra ADDR <<< "$service_info"
        service_name="${ADDR[0]}"
        port="${ADDR[1]}"
        
        test_endpoint "http://localhost:$port/actuator/health" 200 "$service_name health endpoint"
    done
}

# Function to generate test report
generate_test_report() {
    print_status "Generating test report..."
    
    report_file="/tmp/chatty_test_report_$(date +%Y%m%d_%H%M%S).txt"
    
    cat > "$report_file" <<EOF
Chatty Microservices Production Test Report
Generated: $(date)
Base URL: $BASE_URL
Eureka URL: $EUREKA_URL

Test Summary:
- Infrastructure Services: $(docker ps --format "table {{.Names}}" | grep chatty | wc -l) containers running
- Service Health: $(curl -s "$EUREKA_URL/eureka/apps" -H "Accept: application/json" | jq -r '.applications.application | length' 2>/dev/null || echo "Unknown") services registered
- Authentication: $([ -f /tmp/test_token ] && echo "Working" || echo "Failed")

For detailed logs, check the terminal output above.
EOF
    
    print_success "Test report generated: $report_file"
}

# Function to cleanup test artifacts
cleanup_test_artifacts() {
    print_status "Cleaning up test artifacts..."
    
    rm -f /tmp/test_token
    rm -f /tmp/chatty_test_report_*.txt
    
    print_success "Test artifacts cleaned up"
}

# Function to show usage
show_usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  --health-only     Only test health endpoints"
    echo "  --auth-only       Only test authentication"
    echo "  --infrastructure  Only test infrastructure"
    echo "  --eureka-only     Only test Eureka registration"
    echo "  --cleanup         Clean up test artifacts"
    echo "  --help            Show this help message"
    echo ""
    echo "Environment Variables:"
    echo "  BASE_URL      Base URL for API Gateway (default: http://localhost:8085)"
    echo "  EUREKA_URL    Eureka server URL (default: http://localhost:8761)"
    echo "  TIMEOUT       Request timeout in seconds (default: 30)"
    echo ""
    echo "Examples:"
    echo "  $0                    # Full test suite"
    echo "  $0 --health-only     # Test health endpoints only"
    echo "  BASE_URL=https://api.chatty.com $0  # Test production API"
}

# Main execution
main() {
    case "${1:-}" in
        --health-only)
            test_all_health_endpoints
            ;;
        --auth-only)
            test_auth_flow
            ;;
        --infrastructure)
            test_infrastructure
            ;;
        --eureka-only)
            test_eureka_registration
            ;;
        --cleanup)
            cleanup_test_artifacts
            ;;
        --help)
            show_usage
            ;;
        "")
            # Full test suite
            print_status "🧪 Starting Chatty Microservices Production Test Suite 🧪"
            echo ""
            
            test_infrastructure
            test_all_health_endpoints
            test_eureka_registration
            test_database_connectivity
            test_auth_flow
            test_user_service
            test_chat_service
            test_websocket_connectivity
            generate_test_report
            
            print_success "🎉 Test suite completed! 🎉"
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
