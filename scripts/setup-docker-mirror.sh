#!/bin/bash
# ==========================================================
# Docker Hub 国内镜像加速一键配置（在【服务器】上以 root/sudo 运行）
#   用法: sudo bash scripts/setup-docker-mirror.sh
#
#   它会合并写入 /etc/docker/daemon.json 的 registry-mirrors，
#   保留你已有的其他 Docker 配置，然后重启 Docker。
#
#   如需更换镜像源，可先 export DOCKER_MIRRORS="url1 url2 ..." 再运行，
#   默认使用下方列表。
# ==========================================================
set -e

if [ "$(id -u)" -ne 0 ]; then
    echo "请用 root 运行：sudo bash scripts/setup-docker-mirror.sh"
    exit 1
fi

DAEMON_JSON="/etc/docker/daemon.json"

# 默认镜像源列表（清华优先，若失效可自行调整）
if [ -z "$DOCKER_MIRRORS" ]; then
    DOCKER_MIRRORS="
        https://docker.mirrors.tuna.tsinghua.edu.cn
        https://docker.1ms.run
        https://docker.m.daocloud.io
        https://dockerproxy.net
    "
fi

# 组装 JSON 数组（去掉空行/首尾空格，逐个加引号）
MIRROR_LIST=""
while IFS= read -r line; do
    line="$(echo "$line" | xargs)"
    [ -z "$line" ] && continue
    MIRROR_LIST="$MIRROR_LIST    \"$line\",\n"
done <<< "$DOCKER_MIRRORS"
MIRROR_LIST="$(printf "%b" "$MIRROR_LIST" | sed '$ s/,$//')"

# 备份现有配置
if [ -f "$DAEMON_JSON" ]; then
    cp "$DAEMON_JSON" "${DAEMON_JSON}.bak.$(date +%s)"
    echo ">>> 已备份原配置到 ${DAEMON_JSON}.bak.*"
fi

mkdir -p "$(dirname "$DAEMON_JSON")"

cat > "$DAEMON_JSON" <<EOF
{
  "registry-mirrors": [
${MIRROR_LIST}
  ]
}
EOF

echo ">>> 已写入 $DAEMON_JSON :"
cat "$DAEMON_JSON"

echo ">>> 重启 Docker ..."
if command -v systemctl >/dev/null 2>&1 && systemctl list-units --type=service 2>/dev/null | grep -q docker.service; then
    systemctl daemon-reload
    systemctl restart docker
else
    service docker restart
fi

echo ">>> 验证镜像源是否可用（分别测一次拉取元信息）..."
docker pull hello-world >/dev/null 2>&1 && echo "    hello-world 拉取成功 ✔" || echo "    hello-world 拉取失败，请换镜像源"

echo ">>> 完成。现在可重新构建："
echo "    bash scripts/bootstrap.sh"
echo "    或：docker compose -f docker-compose.prod.yml build"
