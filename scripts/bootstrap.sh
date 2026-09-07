#!/bin/bash
# ==========================================================
# Food Traceability 一键部署引导脚本（在【服务器】上运行）
#
#   功能：
#     1. 若 .env 不存在，则从 .env.example 复制生成
#     2. 校验 .env 是否仍有未替换的 CHANGE_ME 占位符
#     3. 若缺 certs/，自动生成自签名证书（HTTPS 引导用）
#     4. 若缺 backend/keys/，自动生成 RSA 密钥对
#     5. 调用 scripts/deploy.sh 完成构建与启动
#
#   用法：
#     bash scripts/bootstrap.sh
# ==========================================================
set -e

cd "$(dirname "$0")/.."          # 定位到仓库根目录
ROOT="$(pwd)"
echo "工作目录: $ROOT"

# ---------- 1. 生成 .env ----------
if [ ! -f .env ]; then
    echo ">>> 未找到 .env，正在从 .env.example 复制…"
    cp .env.example .env
    echo ">>> 已生成 .env，请先编辑填入真实密码再运行本脚本。"
    echo "    编辑: nano .env"
    exit 1
fi
echo ">>> .env 已存在"

# ---------- 2. 校验占位符 ----------
if grep -qE "CHANGE_ME|YOUR_(DOMAIN|EMAIL)" .env; then
    echo ""
    echo "!! 错误：.env 中仍存在未替换的占位符："
    grep -nE "CHANGE_ME|YOUR_(DOMAIN|EMAIL)" .env
    echo ""
    echo "    请打开 .env 把上面的 CHANGE_ME 全部改成真实值后重新运行。"
    exit 1
fi

# ---------- 3. 生成 HTTPS 引导证书 ----------
source .env
DOMAIN="${NGINX_HOST:-localhost}"

if [ ! -f certs/server.crt ] || [ ! -f certs/server.key ]; then
    echo ">>> 生成自签名证书用于 HTTPS 引导（域名: $DOMAIN）"
    mkdir -p certs
    openssl req -x509 -nodes -newkey rsa:2048 -days 3650 \
        -keyout certs/server.key -out certs/server.crt \
        -subj "/CN=${DOMAIN}" >/dev/null 2>&1
    chmod 600 certs/server.key
    echo ">>> 自签名证书已生成: certs/{server.crt,server.key}"
else
    echo ">>> certs/ 证书已存在，跳过生成"
fi

# ---------- 4. 生成 RSA 密钥对 ----------
if [ ! -f backend/keys/private.pem ]; then
    echo ">>> 生成区块链 RSA 密钥对"
    mkdir -p backend/keys
    openssl genrsa -out backend/keys/private.pem 2048 2>/dev/null
    openssl rsa -in backend/keys/private.pem -pubout -out backend/keys/public.pem 2>/dev/null
    chmod 600 backend/keys/private.pem
    echo ">>> RSA 密钥已生成: backend/keys/{private.pem,public.pem}"
else
    echo ">>> backend/keys/ RSA 密钥已存在，跳过生成"
fi

# ---------- 5. 校验关键变量非空 ----------
echo ">>> 校验必填环境变量…"
required_vars=("JWT_SECRET" "MYSQL_ROOT_PASSWORD" "MYSQL_PASSWORD" \
               "DEFAULT_ADMIN_PASSWORD" "REDIS_PASSWORD" "ANCHOR_MYSQL_PASSWORD")
missing=0
for var in "${required_vars[@]}"; do
    if [ -z "${!var}" ]; then
        echo "    - 缺少: $var"
        missing=1
    fi
done
if [ "$missing" -eq 1 ]; then
    echo "!! 错误：上述变量在 .env 中为空，请补齐后重试。"
    exit 1
fi
echo ">>> 必填变量完整 ✔"

# ---------- 6. 调起正式部署 ----------
echo ""
echo ">>> 前置检查通过，开始部署（构建并启动）…"
bash scripts/deploy.sh
