#!/bin/bash

# This script starts MySQL locally for development purposes

# Check if MySQL is already running
if pgrep -x "mysqld" > /dev/null; then
    echo "MySQL is already running."
else
    echo "Starting MySQL..."
    
    # For macOS
    if [ -f /usr/local/bin/mysql.server ]; then
        sudo /usr/local/bin/mysql.server start
    # For Linux
    elif [ -f /etc/init.d/mysql ]; then
        sudo /etc/init.d/mysql start
    # For systemd-based systems
    elif command -v systemctl > /dev/null; then
        sudo systemctl start mysql
    else
        echo "Could not determine how to start MySQL on your system."
        echo "Please start MySQL manually and then run the services."
        exit 1
    fi
    
    # Wait for MySQL to start
    echo "Waiting for MySQL to start..."
    sleep 5
    
    # Create the leadreach database if it doesn't exist
    echo "Creating leadreach database if it doesn't exist..."
    mysql -u root -p123456 -e "CREATE DATABASE IF NOT EXISTS leadreach;"
    
    echo "MySQL is now running with the leadreach database."
fi

echo ""
echo "You can now run individual services using:"
echo "./run_services_individually.sh <service-name>"
echo ""
echo "Available services: lead-service, enrichment-service, outreach-service, orchestrator"
