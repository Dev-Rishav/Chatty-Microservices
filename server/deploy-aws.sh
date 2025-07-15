#!/bin/bash

# AWS Production Deployment Script for Chatty Microservices
# This script deploys the containerized services to AWS ECS/ECR

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
PROJECT_NAME="chatty-microservices"
AWS_REGION="${AWS_REGION:-us-east-1}"
ECR_REPOSITORY_PREFIX="${ECR_REPOSITORY_PREFIX:-chatty}"
VERSION="${VERSION:-1.0.0}"
CLUSTER_NAME="${CLUSTER_NAME:-chatty-cluster}"

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

# Function to check AWS CLI and Docker
check_aws_dependencies() {
    print_status "Checking AWS dependencies..."
    
    # Check AWS CLI
    if ! command -v aws &> /dev/null; then
        print_error "AWS CLI is not installed. Please install AWS CLI first."
        exit 1
    fi
    
    # Check Docker
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed. Please install Docker first."
        exit 1
    fi
    
    # Check AWS credentials
    if ! aws sts get-caller-identity &> /dev/null; then
        print_error "AWS credentials not configured. Please run 'aws configure' first."
        exit 1
    fi
    
    print_success "AWS dependencies are available"
}

# Function to create ECR repositories
create_ecr_repositories() {
    print_status "Creating ECR repositories..."
    
    services=(
        "eureka-server"
        "auth-service"
        "user-service"
        "chat-service"
        "notification-service"
        "payment-service"
        "api-gateway"
    )
    
    for service in "${services[@]}"; do
        repository_name="${ECR_REPOSITORY_PREFIX}-${service}"
        
        # Check if repository exists
        if aws ecr describe-repositories --repository-names "$repository_name" --region "$AWS_REGION" &> /dev/null; then
            print_status "ECR repository $repository_name already exists"
        else
            print_status "Creating ECR repository: $repository_name"
            aws ecr create-repository \
                --repository-name "$repository_name" \
                --region "$AWS_REGION" \
                --image-scanning-configuration scanOnPush=true
            print_success "Created ECR repository: $repository_name"
        fi
    done
}

# Function to build and push Docker images
build_and_push_images() {
    print_status "Building and pushing Docker images to ECR..."
    
    # Get ECR login
    aws ecr get-login-password --region "$AWS_REGION" | docker login --username AWS --password-stdin "$(aws sts get-caller-identity --query Account --output text).dkr.ecr.$AWS_REGION.amazonaws.com"
    
    services=(
        "eureka-server:EurekaServer"
        "auth-service:AuthService"
        "user-service:userservice"
        "chat-service:chatservice"
        "notification-service:notificationservice"
        "payment-service:PaymentService"
        "api-gateway:Api_Gateway"
    )
    
    for service_info in "${services[@]}"; do
        IFS=':' read -ra ADDR <<< "$service_info"
        service_name="${ADDR[0]}"
        service_dir="${ADDR[1]}"
        
        repository_name="${ECR_REPOSITORY_PREFIX}-${service_name}"
        ecr_uri="$(aws sts get-caller-identity --query Account --output text).dkr.ecr.$AWS_REGION.amazonaws.com/$repository_name"
        
        print_status "Building and pushing $service_name..."
        
        # Build Docker image
        cd "$service_dir"
        docker build -t "$repository_name:$VERSION" .
        docker tag "$repository_name:$VERSION" "$ecr_uri:$VERSION"
        docker tag "$repository_name:$VERSION" "$ecr_uri:latest"
        
        # Push to ECR
        docker push "$ecr_uri:$VERSION"
        docker push "$ecr_uri:latest"
        
        cd ..
        print_success "Pushed $service_name to ECR"
    done
}

# Function to create ECS cluster
create_ecs_cluster() {
    print_status "Creating ECS cluster..."
    
    # Check if cluster exists
    if aws ecs describe-clusters --clusters "$CLUSTER_NAME" --region "$AWS_REGION" &> /dev/null; then
        print_status "ECS cluster $CLUSTER_NAME already exists"
    else
        print_status "Creating ECS cluster: $CLUSTER_NAME"
        aws ecs create-cluster \
            --cluster-name "$CLUSTER_NAME" \
            --region "$AWS_REGION" \
            --capacity-providers EC2 FARGATE \
            --default-capacity-provider-strategy capacityProvider=FARGATE,weight=1
        print_success "Created ECS cluster: $CLUSTER_NAME"
    fi
}

# Function to create task definitions
create_task_definitions() {
    print_status "Creating ECS task definitions..."
    
    # Create task definitions directory
    mkdir -p aws-task-definitions
    
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
        
        repository_name="${ECR_REPOSITORY_PREFIX}-${service_name}"
        ecr_uri="$(aws sts get-caller-identity --query Account --output text).dkr.ecr.$AWS_REGION.amazonaws.com/$repository_name:latest"
        
        # Create task definition JSON
        cat > "aws-task-definitions/${service_name}-task-definition.json" <<EOF
{
    "family": "${service_name}",
    "networkMode": "awsvpc",
    "requiresCompatibilities": ["FARGATE"],
    "cpu": "512",
    "memory": "1024",
    "executionRoleArn": "arn:aws:iam::$(aws sts get-caller-identity --query Account --output text):role/ecsTaskExecutionRole",
    "containerDefinitions": [
        {
            "name": "${service_name}",
            "image": "${ecr_uri}",
            "essential": true,
            "portMappings": [
                {
                    "containerPort": ${port},
                    "protocol": "tcp"
                }
            ],
            "environment": [
                {
                    "name": "SPRING_PROFILES_ACTIVE",
                    "value": "prod"
                }
            ],
            "logConfiguration": {
                "logDriver": "awslogs",
                "options": {
                    "awslogs-group": "/ecs/${service_name}",
                    "awslogs-region": "${AWS_REGION}",
                    "awslogs-stream-prefix": "ecs"
                }
            },
            "healthCheck": {
                "command": [
                    "CMD-SHELL",
                    "curl -f http://localhost:${port}/actuator/health || exit 1"
                ],
                "interval": 30,
                "timeout": 5,
                "retries": 3,
                "startPeriod": 60
            }
        }
    ]
}
EOF
        
        # Register task definition
        aws ecs register-task-definition \
            --cli-input-json "file://aws-task-definitions/${service_name}-task-definition.json" \
            --region "$AWS_REGION"
        
        print_success "Created task definition for $service_name"
    done
}

# Function to create CloudWatch log groups
create_log_groups() {
    print_status "Creating CloudWatch log groups..."
    
    services=(
        "eureka-server"
        "auth-service"
        "user-service"
        "chat-service"
        "notification-service"
        "payment-service"
        "api-gateway"
    )
    
    for service in "${services[@]}"; do
        log_group_name="/ecs/${service}"
        
        # Check if log group exists
        if aws logs describe-log-groups --log-group-name-prefix "$log_group_name" --region "$AWS_REGION" | grep -q "$log_group_name"; then
            print_status "Log group $log_group_name already exists"
        else
            print_status "Creating log group: $log_group_name"
            aws logs create-log-group \
                --log-group-name "$log_group_name" \
                --region "$AWS_REGION"
            print_success "Created log group: $log_group_name"
        fi
    done
}

# Function to create RDS instance
create_rds_instance() {
    print_status "Creating RDS PostgreSQL instance..."
    
    db_instance_identifier="chatty-postgres-prod"
    
    # Check if RDS instance exists
    if aws rds describe-db-instances --db-instance-identifier "$db_instance_identifier" --region "$AWS_REGION" &> /dev/null; then
        print_status "RDS instance $db_instance_identifier already exists"
    else
        print_status "Creating RDS instance: $db_instance_identifier"
        aws rds create-db-instance \
            --db-instance-identifier "$db_instance_identifier" \
            --db-instance-class db.t3.micro \
            --engine postgres \
            --engine-version 15.3 \
            --master-username chatty_user \
            --master-user-password "${DB_PASSWORD}" \
            --allocated-storage 20 \
            --db-name chatty_db \
            --backup-retention-period 7 \
            --storage-encrypted \
            --region "$AWS_REGION"
        print_success "Created RDS instance: $db_instance_identifier"
    fi
}

# Function to create ElastiCache Redis cluster
create_redis_cluster() {
    print_status "Creating ElastiCache Redis cluster..."
    
    cache_cluster_id="chatty-redis-prod"
    
    # Check if Redis cluster exists
    if aws elasticache describe-cache-clusters --cache-cluster-id "$cache_cluster_id" --region "$AWS_REGION" &> /dev/null; then
        print_status "Redis cluster $cache_cluster_id already exists"
    else
        print_status "Creating Redis cluster: $cache_cluster_id"
        aws elasticache create-cache-cluster \
            --cache-cluster-id "$cache_cluster_id" \
            --cache-node-type cache.t3.micro \
            --engine redis \
            --num-cache-nodes 1 \
            --region "$AWS_REGION"
        print_success "Created Redis cluster: $cache_cluster_id"
    fi
}

# Function to show deployment summary
show_aws_summary() {
    print_success "🚀 AWS Deployment Summary 🚀"
    echo ""
    echo "ECR Repositories:"
    
    services=(
        "eureka-server"
        "auth-service"
        "user-service"
        "chat-service"
        "notification-service"
        "payment-service"
        "api-gateway"
    )
    
    for service in "${services[@]}"; do
        repository_name="${ECR_REPOSITORY_PREFIX}-${service}"
        ecr_uri="$(aws sts get-caller-identity --query Account --output text).dkr.ecr.$AWS_REGION.amazonaws.com/$repository_name"
        echo "  • $service: $ecr_uri"
    done
    
    echo ""
    echo "ECS Cluster: $CLUSTER_NAME"
    echo "Region: $AWS_REGION"
    echo ""
    echo "Next Steps:"
    echo "1. Create VPC and subnets"
    echo "2. Create Application Load Balancer"
    echo "3. Create ECS services"
    echo "4. Configure DNS"
    echo "5. Set up SSL certificates"
    echo ""
    print_success "AWS resources created successfully! 🎉"
}

# Function to show usage
show_usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  --ecr-only      Only create ECR repositories"
    echo "  --build-push    Build and push images to ECR"
    echo "  --infrastructure Create AWS infrastructure (RDS, Redis)"
    echo "  --ecs-setup     Set up ECS cluster and task definitions"
    echo "  --help          Show this help message"
    echo ""
    echo "Environment Variables:"
    echo "  AWS_REGION              AWS region (default: us-east-1)"
    echo "  ECR_REPOSITORY_PREFIX   ECR repository prefix (default: chatty)"
    echo "  VERSION                 Image version (default: 1.0.0)"
    echo "  CLUSTER_NAME            ECS cluster name (default: chatty-cluster)"
    echo "  DB_PASSWORD             Database password (required for RDS)"
    echo ""
    echo "Examples:"
    echo "  $0                      # Full AWS deployment"
    echo "  $0 --ecr-only          # Create ECR repositories only"
    echo "  $0 --build-push        # Build and push images"
    echo "  $0 --infrastructure    # Create RDS and Redis"
}

# Main execution
main() {
    case "${1:-}" in
        --ecr-only)
            check_aws_dependencies
            create_ecr_repositories
            ;;
        --build-push)
            check_aws_dependencies
            create_ecr_repositories
            build_and_push_images
            ;;
        --infrastructure)
            check_aws_dependencies
            create_rds_instance
            create_redis_cluster
            ;;
        --ecs-setup)
            check_aws_dependencies
            create_ecs_cluster
            create_log_groups
            create_task_definitions
            ;;
        --help)
            show_usage
            ;;
        "")
            # Full AWS deployment
            check_aws_dependencies
            create_ecr_repositories
            build_and_push_images
            create_rds_instance
            create_redis_cluster
            create_ecs_cluster
            create_log_groups
            create_task_definitions
            show_aws_summary
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
