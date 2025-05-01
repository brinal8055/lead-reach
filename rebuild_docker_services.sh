#!/bin/bash

echo "Stopping all Docker services..."
docker-compose down

echo "Rebuilding Docker images with no cache..."
docker-compose build --no-cache

echo "Starting all Docker services..."
docker-compose up -d

echo "All services have been rebuilt and restarted."
echo "You can check the logs with: docker-compose logs -f"
