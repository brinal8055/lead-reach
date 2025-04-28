#!/bin/bash

# This script stops all running services

echo "Stopping all running services..."

# Find and kill Java processes running the services
for service in lead-service enrichment-service outreach-service orchestrator; do
    echo "Looking for running $service processes..."
    pids=$(ps aux | grep "$service-0.1-all.jar" | grep -v grep | awk '{print $2}')
    
    if [ -n "$pids" ]; then
        echo "Found $service running with PID(s): $pids"
        for pid in $pids; do
            echo "Stopping process $pid..."
            kill $pid
            echo "Process $pid stopped."
        done
    else
        echo "No running $service processes found."
    fi
done

echo "All services have been stopped."
echo ""
echo "To stop MySQL as well, you can use:"
echo "  For macOS: sudo /usr/local/bin/mysql.server stop"
echo "  For Linux: sudo systemctl stop mysql"
echo "  Or the appropriate command for your system."
