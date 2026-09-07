#!/bin/bash
set -e

echo "============================================"
echo " Let's Encrypt 证书初始化 + 自动续期"
echo "============================================"

# 检查环境变量
if [ -z "$NGINX_HOST" ]; then
    echo "请设置 NGINX_HOST（域名）"
    echo "用法: NGINX_HOST=yourdomain.com LETSENCRYPT_EMAIL=you@email.com bash init-letsencrypt.sh"
    exit 1
fi

if [ -z "$LETSENCRYPT_EMAIL" ]; then
    echo "请设置 LETSENCRYPT_EMAIL"
    exit 1
fi

COMPOSE_FILE="docker-compose.prod.yml"

# 1. 临时启动 edge（用自签名引导，仅开 80 端口）
echo ">>> 启动 edge（只开 80 端口用于 ACME 验证）"
docker compose -f $COMPOSE_FILE up -d edge
sleep 3

# 2. 用 certbot 申请证书
echo ">>> 申请 Let's Encrypt 证书 - 域名: $NGINX_HOST"
docker compose -f $COMPOSE_FILE run --rm --no-deps \
  -v letsencrypt_etc:/etc/letsencrypt \
  -v certbot_www:/var/www/certbot \
  certbot/certbot certonly --webroot -w /var/www/certbot \
    --cert-name food-traceability \
    -d "$NGINX_HOST" \
    --email "$LETSENCRYPT_EMAIL" \
    --agree-tos --non-interactive

echo ">>> 证书申请成功！重启 edge 加载正式证书"
docker compose -f $COMPOSE_FILE down edge
docker compose -f $COMPOSE_FILE up -d edge

# 3. 设置 cron 自动续期（每 60 天）
CRON_JOB="0 3 1 */2 * cd $(pwd) && docker compose -f $COMPOSE_FILE run --rm --no-deps certbot >/dev/null 2>&1 && docker compose -f $COMPOSE_FILE restart edge >/dev/null 2>&1"
(crontab -l 2>/dev/null | grep -v "init-letsencrypt\|letsencrypt\|certbot"; echo "$CRON_JOB") | crontab -

echo "============================================"
echo " 完成！"
echo " - 证书路径: /etc/letsencrypt/live/food-traceability/"
echo " - 自动续期: 每 2 个月检查一次（cron）"
echo " - 手动续期: docker compose run --rm certbot"
echo "============================================"
