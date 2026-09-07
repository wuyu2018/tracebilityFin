#!/bin/bash
set -e

echo "======================================"
echo "Food Traceability System Deployment"
echo "======================================"

# Check if .env file exists
if [ ! -f .env ]; then
    echo "Error: .env file not found!"
    echo "Please copy .env.example to .env and configure it."
    exit 1
fi

# Load environment variables
source .env

# Determine if distributed mode is needed
COMPOSE_PROFILE=""
if [ "${CONSENSUS_GRPC_ENABLED}" = "true" ]; then
    COMPOSE_PROFILE="--profile distributed"
fi

# Validate required variables
required_vars=("JWT_SECRET" "MYSQL_ROOT_PASSWORD" "MYSQL_DATABASE" "MYSQL_USER" "MYSQL_PASSWORD")
for var in "${required_vars[@]}"; do
    if [ -z "${!var}" ]; then
        echo "Error: $var is not set in .env"
        exit 1
    fi
done

COMPOSE_FILE="docker-compose.prod.yml"

echo "Stopping existing containers..."
docker-compose -f $COMPOSE_FILE $COMPOSE_PROFILE down || true

echo "Building images..."
docker-compose -f $COMPOSE_FILE build --no-cache

echo "Cleaning up old images and build cache..."
docker system prune -f

echo "Starting services..."
docker-compose -f $COMPOSE_FILE $COMPOSE_PROFILE up -d

echo "Waiting for backend to be ready..."
for i in {1..30}; do
    if curl -sf http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo "Backend is ready!"
        break
    fi
    echo "Waiting for backend... ($i/30)"
    sleep 2
done

echo "======================================"
echo "Deployment completed!"
echo "Frontend: http://localhost:${FRONTEND_EXPOSED_PORT:-80}"
echo "Backend:  http://localhost:8080"
echo "======================================"

# 设置每日数据库备份 cron（凌晨 3 点，保留 7 天）
CRON_BACKUP="0 3 * * * cd $(pwd) && bash scripts/backup.sh >/dev/null 2>&1"
(crontab -l 2>/dev/null | grep -v "backup.sh"; echo "$CRON_BACKUP") | crontab -
echo "[deploy] Daily database backup cron installed"

echo ""
echo "Useful commands:"
echo "  docker-compose logs -f backend  # View backend logs"
echo "  docker-compose logs -f frontend # View frontend logs"
echo "  docker-compose down             # Stop services"
echo "  docker-compose restart           # Restart services"
