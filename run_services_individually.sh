#!/bin/bash

# This script allows running each microservice individually
# Make sure MySQL is running before executing this script

# Set common environment variables
export JDBC_URL="jdbc:mysql://localhost:3306/leadreach"
export JDBC_USER="root"
export JDBC_PASSWORD="123456"

# Function to run a specific service
run_service() {
    local service_name=$1
    echo "Starting $service_name..."
    
    # Set service-specific environment variables
    case $service_name in
        "orchestrator")
            # Orchestrator needs to know where other services are running
            export LEAD_SERVICE_HOST="localhost"
            export LEAD_SERVICE_PORT="50055"
            export ENRICHMENT_SERVICE_HOST="localhost"
            export ENRICHMENT_SERVICE_PORT="50052"
            export OUTREACH_SERVICE_HOST="localhost"
            export OUTREACH_SERVICE_PORT="50053"
            ;;
        "lead-service")
            # Lead service port - use a different port to avoid conflicts
            export GRPC_PORT="50055"
            export SERVER_PORT="8081"
            ;;
        "enrichment-service")
            # Enrichment service port
            export GRPC_PORT="50052"
            export SERVER_PORT="8082"
            ;;
        "outreach-service")
            # Outreach service port
            export GRPC_PORT="50053"
            export SERVER_PORT="8083"
            ;;
        *)
            echo "Unknown service: $service_name"
            exit 1
            ;;
    esac
    
    # Build the service using the root gradlew
    ./gradlew :services:$service_name:build -x test
    
    # Run the service
    java -jar services/$service_name/build/libs/$service_name-0.1-all.jar
}

# Check if a specific service was requested
if [ $# -eq 0 ]; then
    echo "Usage: $0 <service-name>"
    echo "Available services: lead-service, enrichment-service, outreach-service, orchestrator"
    exit 1
fi

# Run the requested service
run_service $1
