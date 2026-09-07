# 食品安全溯源系统 (Food Traceability System)

基于区块链与多智能体系统的食品全链路溯源平台。

## 目录

- [项目概述](#项目概述)
- [技术栈](#技术栈)
- [系统架构](#系统架构)
- [快速启动](#快速启动)
- [模块说明](#模块说明)
- [API 文档](#api-文档)
- [开发指南](#开发指南)
- [维护指南](#维护指南)

---

## 项目概述

本系统实现从**原料采购 → 生产加工 → 质检 → 仓储 → 运输 → 销售**的全链路溯源，核心特性：

- **消费者防伪查询**：输入防伪码或扫描二维码，查看产品全生命周期信息
- **区块链存证**：SHA-256 哈希链 + RSA-2048 签名，数据不可篡改
- **多智能体协作**：4 个 Agent 分别负责生产/流通/销售环节，PBFT 共识达成信任
- **双链结构**：MATERIAL 链（原料）+ BATCH 链（批次），交叉引用验证
- **混合加密**：AES-256-GCM 加密原始数据 + RSA 加密密钥，权限访问控制
- **双数据库**：主库 + 独立锚定库，每日快照归档

---

## 技术栈

| 层次 | 技术 |
|------|------|
| **后端** | Java 17, Spring Boot 3.2.1, Spring Data JPA |
| **前端** | Vue 3, Element Plus, Vite 5 |
| **数据库** | MySQL 8.0 (主库 + 锚定库), Redis 7 |
| **区块链** | SHA-256 哈希链, RSA-2048 签名, PBFT 共识, gRPC |
| **加密** | AES-256-GCM, RSA-OAEP, SHA256withRSA |
| **构建** | Maven, Docker Compose |
| **通信** | REST API, gRPC, Redis Pub/Sub |

---

## 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                       前端 (Vue 3)                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  Home/Verify  │  │  Complaint   │  │  Admin Panel │      │
│  │  (消费者页面)   │  │  (投诉页面)    │  │  (管理后台)    │      │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘      │
└─────────┼─────────────────┼──────────────────┼──────────────┘
          │                 │                  │
    ┌─────┴─────────────────┴──────────────────┴─────┐
    │              Nginx 反向代理                      │
    └─────────────────────┬───────────────────────────┘
                          │
┌─────────────────────────┴───────────────────────────────┐
│                   后端 (Spring Boot 3.2)                  │
│                                                          │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐   │
│  │Controller │ │ Service  │ │  Agent   │ │Smart     │   │
│  │ 层       │ │  层      │ │  系统     │ │Contract  │   │
│  └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘   │
│       │            │            │              │          │
│  ┌────┴────────────┴────────────┴──────────────┴────┐    │
│  │             领域事件 (Domain Events)               │    │
│  └─────────────────────┬───────────────────────────┘    │
│                        │                                 │
│  ┌─────────────────────┴───────────────────────────┐    │
│  │       AgentBlockchainService (上链服务)          │    │
│  │  PBFT 共识 → 加密 → 区块头 → 区块 → offchain    │    │
│  └─────────────────────┬───────────────────────────┘    │
│                        │                                 │
│  ┌─────────────────────┴───────────────────────────┐    │
│  │              数据存储层                           │    │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐         │    │
│  │  │ 主库 MySQL│ │锚定库 MySQL│ │ Redis 7 │         │    │
│  │  │blockchain│ │blockchain│ │ Bloom   │         │    │
│  │  │_log 等   │ │_anchor   │ │ Filter  │         │    │
│  │  └──────────┘ └──────────┘ └──────────┘         │    │
│  └─────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────┘
```

### 核心分层

| 层次 | 说明 |
|------|------|
| **Controller 层** | REST API 接口，包括公共消费者接口和 Admin 管理接口 |
| **Service 层** | 业务逻辑 + ApplicationService (DDD) |
| **Agent 系统** | 4 个智能体 + PBFT 共识 + 4 份智能合约 + 双账本 |
| **领域事件** | 业务操作完成后发布事件，触发上链和 Agent 协作 |
| **数据存储** | 链上(blockchain_log) + 链下(offchain_storage 加密) + Redis 缓存 |

---

## 快速启动

### 前置条件

- JDK 17+
- Node.js 20+
- MySQL 8.0 (主库 + 锚定库)
- Redis 7
- Maven 3.8+

### 本地开发

```bash
# 1. 启动基础设施
docker-compose up -d mysql redis mysql-anchor

# 2. 启动后端
cd backend
mvn spring-boot:run

# 3. 启动前端
cd frontend
npm install
npm run dev
```

### 生产部署

```bash
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

---

## 模块说明

### 1. 消费者溯源 (Consumer Traceability)

**入口**：前端 `/verify` → 输入防伪码 → `GET /api/v2/trace/verify?code=xxx`

**数据链路**：

```
防伪码 (SecurityCode)
  └→ 生产批次 (ProductionBatch)
       ├→ 产品 (Product)
       ├→ 原料采购 (MaterialPurchase)  ← TraceabilityLink / BatchMaterialRelation
       ├→ 质检 (Inspection)             ← TraceabilityLink
       ├→ 仓储 (Storage)                ← TraceabilityLink
       └→ 运输销售 (TransportSale)      ← TraceabilityLink
```

**防伪机制**：首次查询激活码并记录时间，重复查询提示"可能是伪品"。

### 2. 区块链监控 (Blockchain Monitor)

**入口**：后台 `/manage/blockchain` → `GET /api/blockchain/monitor/summary`

**校验内容**：
- `previous_hash` 连续性（链式结构完整性）
- `current_hash` 重算校验（数据未被篡改）
- RSA 签名验证（签名有效性）

### 3. 数据上链 (Data On-Chain)

**触发方式**：6 个领域事件 → `@TransactionalEventListener` → `AgentBlockchainService.appendBlockWithConsensus()`

**上链流程**：
```
业务操作 → 领域事件 → PBFT 共识 → AES 加密数据 → 存入 offchain_storage
                                → 计算哈希 → 签名 → 存入 blockchain_log + block_header
```

### 4. 多智能体系统 (Multi-Agent System)

| Agent | 职责 |
|-------|------|
| CA-Agent | 证书颁发、身份管理、吊销 |
| Production-Agent | 原料管理、生产批次、质检 |
| Circulation-Agent | 仓储、运输管理 |
| Sales-Agent | 销售、订单管理 |

**协作模式**：业务事件 → 对应 Agent 执行业务操作 + 更新信用分 → PBFT 共识 → 上链

### 5. 数据存储架构

| 存储层 | 用途 |
|--------|------|
| `blockchain_log` | 链上区块记录（哈希 + 签名 + 引用） |
| `block_header` | 区块头（Merkle 根 + Bloom Filter + 元数据） |
| `offchain_storage` | 加密原始数据（AES-256-GCM） |
| `blockchain_anchor` | 每日快照锚定（独立数据库） |
| `blockchain_retry_task` | 上链失败重试任务 |
| Redis | Bloom Filter + 缓存 + Pub/Sub |

---

## API 文档

详见 [`api.md`](api.md)，主要端点：

### 公开接口（无需认证）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v2/trace/verify?code=` | 防伪码查询 |
| GET | `/api/v2/trace/batch/{batchNumber}` | 批次号查询 |
| POST | `/api/complaint` | 提交投诉 |

### 管理接口（需 JWT 认证）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/products` | 创建产品 |
| POST | `/api/v2/materials` | 创建原料品种 |
| POST | `/api/v2/material-purchases` | 创建采购单 |
| POST | `/api/v2/batches` | 创建生产批次 |
| POST | `/api/v2/inspections` | 完成质检 |
| POST | `/api/v2/storage` | 录入仓储 |
| POST | `/api/v2/transport-sales` | 录入运输销售 |
| POST | `/api/batches/{id}/security-codes` | 生成防伪码 |
| GET | `/api/blockchain/monitor/summary` | 区块链监控 |
| GET | `/api/agent/list` | Agent 列表 |

---

## 开发指南

### 代码结构

```
backend/
├── src/main/java/com/foodtraceability/
│   ├── config/          # Spring 配置
│   ├── controller/      # REST 控制器
│   ├── service/         # 业务服务
│   ├── repository/      # JPA 仓库
│   ├── entity/          # JPA 实体
│   ├── dto/             # 数据传输对象
│   ├── security/        # 加密/安全组件
│   ├── agent/           # 多智能体系统
│   │   ├── core/        # Agent 接口 + 协调器
│   │   ├── impl/        # 4 个 Agent 实现
│   │   ├── consensus/   # PBFT 共识 + gRPC
│   │   ├── contract/    # 4 份智能合约
│   │   ├── credential/  # CA + 证书管理
│   │   ├── ledger/      # 双账本 + 声誉
│   │   └── service/     # 区块链上链服务
│   ├── traceability/    # DDD 溯源域
│   └── anchor/          # 锚定数据库
└── src/main/resources/
    └── sql/             # 参考 SQL 脚本

frontend/
├── src/
│   ├── views/consumer/  # 消费者页面
│   ├── views/admin/     # 管理后台
│   ├── api/             # API 调用
│   └── router/          # 路由配置
```

### 关键业务流程

参见各模块分析文档：
- [消费者溯源流程](#1-消费者溯源-consumer-traceability)
- [数据上链流程](#3-数据上链-data-on-chain)
- [智能体协作](#4-多智能体系统-multi-agent-system)

---

## 维护指南

### 数据库迁移

迁移脚本位于 `scripts/` 目录，按日期命名：

```bash
# 执行迁移
mysql -u root -p traceability < scripts/migration-YYYY-MM-DD-xxx.sql
```

### 常见问题

| 问题 | 排查方向 |
|------|---------|
| 防伪码查询不到 | 检查 `security_code` 表是否存在该 code，检查 `batch_id` 关联 |
| 溯源数据缺失某环节 | 检查 `traceability_link` 表是否有对应 entity_type 记录 |
| 区块链监控显示损坏 | 查看 `brokenBlocks` 详情中的 errors（previous_hash mismatch / current_hash mismatch / invalid signature） |
| 上链失败 | 检查 `blockchain_retry_task` 表 FAILED 状态的记录和 last_error |
| Agent 不可用 | 检查 agent 状态是否为 CERTIFIED/ACTIVE，信用分是否 ≥ 50 |
| 数据加密后无法读取 | 检查 Agent 私钥是否匹配存入的 encrypted_aes_key |
| 重启后数据丢失 | Agent metadata/证书/账本为内存存储，重启后需重新初始化。链上数据和 offchain 数据持久化在 MySQL |
| 共识失败 | 检查 gRPC 节点连接状态、PBFT 超时日志、节点数量是否 ≥ 4 |

### 清理重建

```bash
# 清空所有区块链相关数据，重启后自动重新上链（从业务表恢复）
mysql -u root -p traceability < scripts/clean-and-rebuild.sql
```
