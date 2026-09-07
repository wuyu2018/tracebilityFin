-- ============================================================
-- 食品安全溯源数据存储方案 - 数据库初始化脚本
-- 参考: 《食品安全质量检测学报》第 17 卷
-- 关联文档: docs/data-storage-design.md
-- ============================================================

-- 注: 主要业务表由 JPA ddl-auto: update 自动管理。
-- 此脚本仅作参考，列出核心存储相关表的结构。

-- ----------------------------
-- 1. 区块链日志表 (链上)
-- ----------------------------
CREATE TABLE IF NOT EXISTS blockchain_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    chain_type VARCHAR(30) NOT NULL COMMENT 'MATERIAL/BATCH/TRANSPORT/SALES',
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    action VARCHAR(20) NOT NULL,
    previous_hash VARCHAR(128),
    current_hash VARCHAR(128) NOT NULL,
    data_snapshot TEXT COMMENT '数据快照/摘要 (已弃用，保留兼容)',
    data_hash VARCHAR(64) COMMENT '原始数据 SHA-256 哈希',
    offchain_ref VARCHAR(200) COMMENT '链下存储引用(foodId)',
    signature VARCHAR(512) NOT NULL,
    timestamp DATETIME(6) NOT NULL,
    operator_id BIGINT,
    ref_master_chain_hash VARCHAR(128),
    batch_id BIGINT,
    block_header_id BIGINT COMMENT '关联区块头 ID',
    INDEX idx_bl_chain_type (chain_type),
    INDEX idx_bl_entity (entity_type, entity_id),
    INDEX idx_bl_timestamp (timestamp),
    INDEX idx_bl_batch_id (batch_id),
    INDEX idx_bl_data_hash (data_hash),
    INDEX idx_bl_block_header (block_header_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='区块链日志表 (链上数据摘要)';

-- 按时间分区 (可选)
-- ALTER TABLE blockchain_log
-- PARTITION BY RANGE (YEAR(timestamp)) (
--     PARTITION p2025 VALUES LESS THAN (2026),
--     PARTITION p2026 VALUES LESS THAN (2027)
-- );

-- 覆盖索引 (可选)
-- CREATE INDEX idx_blockchain_query
-- ON blockchain_log(chain_type, entity_type, entity_id, timestamp);

-- ----------------------------
-- 2. 链下存储引用表
-- ----------------------------
CREATE TABLE IF NOT EXISTS offchain_storage (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    food_id VARCHAR(100) UNIQUE NOT NULL COMMENT '食品唯一标识',
    data_hash VARCHAR(64) NOT NULL COMMENT '原始数据 SHA-256 哈希',
    storage_type VARCHAR(20) NOT NULL COMMENT 'REDIS/LOCAL_FILE/OSS/DATABASE',
    storage_key VARCHAR(200) NOT NULL COMMENT '存储位置/键',
    encryption_method VARCHAR(50) COMMENT '加密算法 (如 AES-256-GCM)',
    encrypted_data TEXT COMMENT '加密后的原始数据',
    encrypted_aes_key TEXT COMMENT '用接收方公钥加密的 AES 密钥',
    owner_agent_id BIGINT NOT NULL COMMENT '数据所有者 Agent ID',
    access_policy JSON COMMENT '访问控制策略 (RBAC JSON)',
    created_at DATETIME NOT NULL,
    expires_at DATETIME,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    INDEX idx_offchain_food_id (food_id),
    INDEX idx_offchain_storage (storage_type, storage_key),
    INDEX idx_offchain_owner (owner_agent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='链下存储引用表';

-- ----------------------------
-- 3. 智能合约状态表
-- ----------------------------
CREATE TABLE IF NOT EXISTS smart_contract_state (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contract_id VARCHAR(50) NOT NULL,
    contract_type VARCHAR(50) NOT NULL COMMENT 'PERMISSION/TRANSACTION/DATA/STATE',
    state_key VARCHAR(200) NOT NULL,
    state_value TEXT,
    version BIGINT NOT NULL DEFAULT 1,
    updated_at DATETIME NOT NULL,
    UNIQUE INDEX idx_contract_state (contract_id, state_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能合约状态表';

-- ----------------------------
-- 4. Agent 身份表
-- ----------------------------
CREATE TABLE IF NOT EXISTS agent_identity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    agent_id VARCHAR(50) UNIQUE NOT NULL,
    agent_type VARCHAR(30) NOT NULL COMMENT 'PRODUCTION/CIRCULATION/SALES/CA',
    certificate_serial VARCHAR(100) COMMENT '证书序列号',
    public_key TEXT COMMENT 'RSA 公钥 (PEM)',
    credit_score BIGINT DEFAULT 100 COMMENT '信誉分',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT 'ACTIVE/SUSPENDED/REVOKED',
    registered_at DATETIME NOT NULL,
    last_active_at DATETIME,
    metadata JSON,
    INDEX idx_agent_type (agent_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Agent 身份表';

-- ----------------------------
-- 5. 区块头表 (链式结构主体)
-- ----------------------------
CREATE TABLE IF NOT EXISTS block_header (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    chain_type VARCHAR(30) NOT NULL COMMENT 'MATERIAL/BATCH/TRANSPORT/SALES',
    block_hash VARCHAR(128) NOT NULL COMMENT '本区块哈希',
    previous_hash VARCHAR(128) COMMENT '上一区块哈希',
    merkle_root VARCHAR(128) COMMENT 'Merkle 根哈希',
    timestamp DATETIME(6) NOT NULL COMMENT '区块生成时间',
    bloom_filter BLOB COMMENT '合并 Bloom Filter',
    metadata_index JSON COMMENT '本块元数据索引',
    tx_count INT COMMENT '本块交易数',
    INDEX idx_bh_chain_type (chain_type),
    INDEX idx_bh_timestamp (timestamp),
    INDEX idx_bh_block_hash (block_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='区块头表';
