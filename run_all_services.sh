#!/bin/bash

# This script starts all services in separate terminals for local development

# First, ensure MySQL is running
./start_mysql.sh

# Function to start a service in a new terminal
start_service() {
    local service_name=$1
    
    # For macOS
    if [[ "$OSTYPE" == "darwin"* ]]; then
        osascript -e "tell app \"Terminal\" to do script \"cd $(pwd) && ./run_services_individually.sh $service_name\""
    # For Linux with gnome-terminal
    elif command -v gnome-terminal &> /dev/null; then
        gnome-terminal -- bash -c "cd $(pwd) && ./run_services_individually.sh $service_name; exec bash"
    # For Linux with xterm
    elif command -v xterm &> /dev/null; then
        xterm -e "cd $(pwd) && ./run_services_individually.sh $service_name" &
    # For Windows with WSL
    elif grep -q Microsoft /proc/version 2>/dev/null; then
        cmd.exe /c "start cmd.exe /k \"cd $(wslpath -w $(pwd)) && wsl ./run_services_individually.sh $service_name\""
    else
        echo "Could not determine how to open a new terminal on your system."
        echo "Please run each service manually in separate terminals:"
        echo "./run_services_individually.sh $service_name"
        return 1
    fi
    
    echo "Started $service_name in a new terminal"
    # Wait a bit to allow the service to start
    sleep 5
}

echo "Starting all services in separate terminals..."

# Start services in order of dependencies
echo "Starting lead-service..."
start_service lead-service

echo "Starting enrichment-service..."
start_service enrichment-service

echo "Starting outreach-service..."
start_service outreach-service

echo "Starting orchestrator..."
start_service orchestrator

echo "All services have been started in separate terminals."
echo "Check the terminal windows for each service to monitor their status."
