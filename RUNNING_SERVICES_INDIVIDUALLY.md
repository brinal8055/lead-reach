# Running Services Individually

This guide explains how to run each microservice individually for development purposes, rather than using Docker Compose to run all services together.

## Prerequisites

- Java 17 or higher
- MySQL 8.0 installed locally
- Gradle

## Option 1: Run All Services at Once

To start all services in separate terminal windows:

```bash
./run_all_services.sh
```

This script will:
1. Start MySQL if it's not already running
2. Launch each service in a separate terminal window
3. Start services in the correct dependency order

## Option 2: Run Services Individually

### Step 1: Start MySQL

Before running any services, make sure MySQL is running. You can use the provided script:

```bash
./start_mysql.sh
```

This script will:
1. Check if MySQL is already running
2. Start MySQL if it's not running
3. Create the `leadreach` database if it doesn't exist

### Step 2: Run Individual Services

You can run each service individually using the provided script:

```bash
./run_services_individually.sh <service-name>
```

Available services:
- `lead-service`
- `enrichment-service`
- `outreach-service`
- `orchestrator`

For example, to run the lead service:

```bash
./run_services_individually.sh lead-service
```

## Service Dependencies

Note that services depend on each other. If you're running a service that depends on another service, make sure the dependency is running first.

Dependencies:
- `orchestrator` depends on `lead-service`, `enrichment-service`, and `outreach-service`
- `lead-service` depends on MySQL
- All services communicate via gRPC

## Service Ports

Each service runs on a specific port:
- `lead-service`: 50051
- `enrichment-service`: 50052
- `outreach-service`: 50053
- `orchestrator`: 50054

## Troubleshooting

If you encounter any issues:

1. Check that MySQL is running and accessible with the credentials in the script (root/password)
2. Ensure the required ports are available
3. Check the logs for any specific error messages
4. Make sure all required dependencies are running

## Stopping Services

To stop all running services:

```bash
./stop_all_services.sh
```

This script will find and terminate all running service processes.

## Switching Back to Docker Compose

If you want to switch back to running all services with Docker Compose:

```bash
docker-compose up
```

This will start all services together in Docker containers.
