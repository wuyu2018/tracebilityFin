#!/bin/bash

echo "======================================"
echo "Food Traceability System Health Check"
echo "======================================"

check_service() {
    local name=$1
    local url=$2
    
    if curl -sf "$url" > /dev/null 2>&1; then
        echo "[OK] $name is healthy"
        return 0
    else
        echo "[FAIL] $name is not responding"
        return 1
    fi
}

echo ""
echo "Checking services..."
check_service "Frontend" "http://localhost:80"
check_service "Backend" "http://localhost:8080/actuator/health"
check_service "MySQL" "http://localhost:3306"

echo ""
echo "Container status:"
docker ps --filter "name=food-traceability" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

echo ""
echo "Resource usage:"
docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}" \
    $(docker ps --filter "name=food-traceability" -q)

echo ""
echo "======================================"
echo "Health check completed"
echo "======================================"
