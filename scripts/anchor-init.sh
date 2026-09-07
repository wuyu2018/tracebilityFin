#!/bin/bash
set -e

ANCHOR_DB="${ANCHOR_MYSQL_DATABASE:-food_traceability_anchor}"

echo "[Anchor Init] Creating database ${ANCHOR_DB}..."
mysql -u root -p"$MYSQL_ROOT_PASSWORD" <<-EOSQL
CREATE DATABASE IF NOT EXISTS \`${ANCHOR_DB}\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EOSQL

echo "[Anchor Init] Creating tables in database ${ANCHOR_DB}..."
mysql -u root -p"$MYSQL_ROOT_PASSWORD" "$ANCHOR_DB" <<-EOSQL
CREATE TABLE IF NOT EXISTS blockchain_anchor (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chain_type VARCHAR(30) NOT NULL,
    batch_id BIGINT,
    current_hash VARCHAR(128) NOT NULL,
    anchor_date DATE NOT NULL,
    created_at DATETIME NOT NULL,
    INDEX idx_ba_batch_date (batch_id, anchor_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    operator VARCHAR(50),
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    action VARCHAR(20) NOT NULL,
    summary VARCHAR(500),
    created_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
EOSQL
echo "[Anchor Init] Tables created."

if [ -n "$ANCHOR_MYSQL_USER" ] && [ -n "$ANCHOR_MYSQL_PASSWORD" ]; then
    echo "[Anchor Init] Creating restricted application user ${ANCHOR_MYSQL_USER}..."
    mysql -u root -p"$MYSQL_ROOT_PASSWORD" <<-EOSQL
    CREATE USER IF NOT EXISTS '${ANCHOR_MYSQL_USER}'@'%' IDENTIFIED BY '${ANCHOR_MYSQL_PASSWORD}';
    GRANT SELECT, INSERT ON \`${ANCHOR_DB}\`.* TO '${ANCHOR_MYSQL_USER}'@'%';
    FLUSH PRIVILEGES;
EOSQL
    echo "[Anchor Init] User ${ANCHOR_MYSQL_USER} created with SELECT, INSERT privileges only."
fi

echo "[Anchor Init] Initialization complete."
