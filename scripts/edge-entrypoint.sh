#!/bin/sh
set -e

LE_PATH="/etc/letsencrypt/live/food-traceability"

# 如果 LE 证书不存在，用自签名证书作为引导
if [ ! -f "$LE_PATH/fullchain.pem" ]; then
    echo "[edge] Let's Encrypt 证书不存在，使用自签名证书作为引导"
    mkdir -p "$LE_PATH"
    cp /etc/nginx/certs/server.crt "$LE_PATH/fullchain.pem"
    cp /etc/nginx/certs/server.key "$LE_PATH/privkey.pem"
fi

echo "[edge] 启动 Nginx"
exec nginx -g "daemon off;"
