#!/bin/bash

# QueueLess — Start all backend services
# Usage: ./start-all.sh
# Each service opens in a new Terminal tab (macOS)

BACKEND_DIR="$(cd "$(dirname "$0")/backend" && pwd)"

echo "================================================"
echo "  QueueLess — Starting all services"
echo "================================================"

# Check MySQL is running
if ! mysqladmin ping -h localhost --silent 2>/dev/null; then
  echo "ERROR: MySQL is not running. Please start MySQL first."
  exit 1
fi

echo "MySQL is running ✓"
echo ""

# Create databases if they don't exist
echo "Creating databases if not exist..."
mysql -u root -proot -e "
  CREATE DATABASE IF NOT EXISTS queueless_user_db;
  CREATE DATABASE IF NOT EXISTS queueless_business_db;
  CREATE DATABASE IF NOT EXISTS queueless_queue_db;
" 2>/dev/null

if [ $? -eq 0 ]; then
  echo "Databases ready ✓"
else
  echo "WARNING: Could not auto-create databases."
  echo "Please run manually:"
  echo "  mysql -u root -p -e 'CREATE DATABASE queueless_user_db;'"
  echo "  mysql -u root -p -e 'CREATE DATABASE queueless_business_db;'"
  echo "  mysql -u root -p -e 'CREATE DATABASE queueless_queue_db;'"
fi

echo ""
echo "Starting services in order..."
echo "(Each service will open in a new Terminal window)"
echo ""

# Function to open a new terminal window and run a service
run_service() {
  local name=$1
  local dir=$2
  osascript -e "
    tell application \"Terminal\"
      do script \"echo '=== $name ===' && cd $dir && mvn spring-boot:run\"
      activate
    end tell
  "
}

# 1. Eureka Server — must start first
echo "1. Starting Eureka Server (port 8761)..."
run_service "Eureka Server" "$BACKEND_DIR/queueless-eureka-server"
echo "   Waiting 20 seconds for Eureka to be ready..."
sleep 20

# 2. API Gateway
echo "2. Starting API Gateway (port 8080)..."
run_service "API Gateway" "$BACKEND_DIR/queueless-api-gateway"
sleep 5

# 3. User Service
echo "3. Starting User Service (port 8081)..."
run_service "User Service" "$BACKEND_DIR/queueless-user-service"
sleep 5

# 4. Business Service
echo "4. Starting Business Service (port 8082)..."
run_service "Business Service" "$BACKEND_DIR/queueless-business-service"
sleep 5

# 5. Queue Service
echo "5. Starting Queue Service (port 8083)..."
run_service "Queue Service" "$BACKEND_DIR/queueless-queue-service"

echo ""
echo "================================================"
echo "  All services started!"
echo ""
echo "  Eureka Dashboard : http://localhost:8761"
echo "  API Gateway      : http://localhost:8080"
echo "  User Swagger     : http://localhost:8081/swagger-ui.html"
echo "  Business Swagger : http://localhost:8082/swagger-ui.html"
echo "  Queue Swagger    : http://localhost:8083/swagger-ui.html"
echo ""
echo "  Wait ~30 seconds for all services to register"
echo "  with Eureka before making API calls."
echo "================================================"
