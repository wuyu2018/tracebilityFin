-- ============================================
-- 数据库迁移脚本：区块头优化 (BlockHeader)
-- 对齐论文区块头结构，将 bloom_filter
-- 和 metadata_index 从 blockchain_log 移到
-- 独立的 block_header 表
-- 日期：2026-05-21
-- ============================================

-- 1. 新建 block_header 表
CREATE TABLE IF NOT EXISTS block_header (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键 ID',
    chain_type VARCHAR(30) NOT NULL COMMENT '链类型：MATERIAL/BATCH/TRANSPORT/SALES',
    block_hash VARCHAR(128) NOT NULL COMMENT '本区块哈希',
    previous_hash VARCHAR(128) COMMENT '上一区块哈希',
    merkle_root VARCHAR(128) COMMENT 'Merkle 根哈希',
    timestamp DATETIME NOT NULL COMMENT '区块生成时间',
    bloom_filter BLOB COMMENT '合并 Bloom Filter',
    metadata_index JSON COMMENT '本块元数据索引',
    tx_count INT COMMENT '本块交易数',
    INDEX idx_bh_chain_type (chain_type),
    INDEX idx_bh_timestamp (timestamp),
    INDEX idx_bh_block_hash (block_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='区块头表 (链式结构主体)';

-- 2. 为 blockchain_log 添加 block_header_id 外键
ALTER TABLE blockchain_log
ADD COLUMN IF NOT EXISTS block_header_id BIGINT COMMENT '关联区块头 ID' AFTER id,
ADD INDEX idx_bl_block_header (block_header_id);

-- 3. 迁移已有数据：为每条 blockchain_log 创建对应的 block_header
-- 按 chain_type 分组，相同 chain_type 的连续记录构成一条链
INSERT INTO block_header (chain_type, block_hash, previous_hash, merkle_root, timestamp, bloom_filter, metadata_index, tx_count)
SELECT
    bl.chain_type,
    SHA2(CONCAT(bl.chain_type, COALESCE(bl.previous_hash, ''), bl.data_hash, bl.timestamp, 1), 256) AS block_hash,
    COALESCE(bl.previous_hash, 'GENESIS') AS previous_hash,
    bl.data_hash AS merkle_root,
    bl.timestamp,
    bl.bloom_filter,
    bl.metadata_index,
    1 AS tx_count
FROM blockchain_log bl
WHERE bl.bloom_filter IS NOT NULL OR bl.metadata_index IS NOT NULL;

-- 4. 将 block_header_id 回写到 blockchain_log
UPDATE blockchain_log bl
JOIN block_header bh ON bh.chain_type = bl.chain_type
    AND (bh.merkle_root = bl.data_hash OR (bh.merkle_root IS NULL AND bl.data_hash IS NULL))
    AND bh.timestamp = bl.timestamp
SET bl.block_header_id = bh.id;

-- 5. 移除 blockchain_log 中的旧字段 (注释掉，确认数据无误后再执行)
-- ALTER TABLE blockchain_log
-- DROP COLUMN IF EXISTS bloom_filter,
-- DROP COLUMN IF EXISTS metadata_index;

-- ============================================
-- 回滚脚本
-- ============================================
--
-- ALTER TABLE blockchain_log
-- ADD COLUMN IF NOT EXISTS bloom_filter BLOB COMMENT 'Bloom Filter 数据' AFTER offchain_ref,
-- ADD COLUMN IF NOT EXISTS metadata_index JSON COMMENT '元数据索引' AFTER bloom_filter;
--
-- UPDATE blockchain_log bl
-- LEFT JOIN block_header bh ON bl.block_header_id = bh.id
-- SET bl.bloom_filter = bh.bloom_filter,
--     bl.metadata_index = bh.metadata_index;
--
-- ALTER TABLE blockchain_log DROP INDEX idx_bl_block_header, DROP COLUMN IF EXISTS block_header_id;
-- DROP TABLE IF EXISTS block_header;
