#!/bin/bash
set -e

BACKUP_DIR="./backup"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="$BACKUP_DIR/food_traceability_backup_$TIMESTAMP.sql.gz"

echo "======================================"
echo "Food Traceability Database Backup"
echo "======================================"

mkdir -p "$BACKUP_DIR"

echo "Creating backup..."
# 使用 MYSQL_PWD 环境变量避免密码在进程列表中明文暴露
docker exec -e MYSQL_PWD="$MYSQL_ROOT_PASSWORD" food-traceability-mysql mysqldump \
    -u root \
    --single-transaction \
    --quick \
    --lock-tables=false \
    $MYSQL_DATABASE | gzip > "$BACKUP_FILE"

echo "Backup created: $BACKUP_FILE"

# Keep only last 7 backups
cd "$BACKUP_DIR"
ls -t food_traceability_backup_*.sql.gz | tail -n +8 | xargs -r rm -f

echo "Backup completed!"
echo "Backup file: $BACKUP_FILE"
ls -lh "$BACKUP_FILE"
