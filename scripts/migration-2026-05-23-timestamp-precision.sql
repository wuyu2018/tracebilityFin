-- ============================================
-- 数据库迁移脚本：修复 timestamp 精度问题
-- 问题：DATETIME 默认 DATETIME(0) 无小数秒
-- 导致 hash 计算时微秒精度的时间戳存入后
-- 精度丢失，重新计算 hash 不匹配
-- 日期：2026-05-23
-- ============================================

-- 1. 修复 blockchain_log.timestamp 精度 (微秒)
ALTER TABLE blockchain_log MODIFY COLUMN timestamp DATETIME(6) NOT NULL COMMENT '时间戳(微秒)';

-- 2. 修复 block_header.timestamp 精度 (微秒)
ALTER TABLE block_header MODIFY COLUMN timestamp DATETIME(6) NOT NULL COMMENT '区块生成时间(微秒)';

-- 3. 清理损坏链数据（batchId=1 和 batchId=2 的 BATCH 链）
--    这些区块的 hash 是用纳秒级时间戳算的，但数据库只存了秒级，无法匹配
DELETE FROM blockchain_log WHERE batch_id IN (1, 2);
DELETE FROM block_header WHERE id NOT IN (SELECT block_header_id FROM blockchain_log WHERE block_header_id IS NOT NULL);

-- ============================================
-- 回滚脚本
-- ============================================
-- ALTER TABLE blockchain_log MODIFY COLUMN timestamp DATETIME NOT NULL;
-- ALTER TABLE block_header MODIFY COLUMN timestamp DATETIME NOT NULL;
