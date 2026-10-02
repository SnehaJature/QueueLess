#!/bin/bash

# QueueLess — Stop all backend services

echo "Stopping all QueueLess services..."

for port in 8761 8080 8081 8082 8083; do
  pid=$(lsof -ti tcp:$port)
  if [ -n "$pid" ]; then
    kill -9 $pid 2>/dev/null
    echo "  Stopped port $port (PID $pid)"
  else
    echo "  Nothing on port $port"
  fi
done

echo "Done."
