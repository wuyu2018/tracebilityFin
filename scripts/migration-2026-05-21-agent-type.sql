-- ============================================
-- 数据库迁移脚本：Admin 增加 agent_type
-- 支持前端角色分流（生产方/流通方/销售方）
-- 日期：2026-05-21
-- ============================================

ALTER TABLE admin
ADD COLUMN IF NOT EXISTS agent_type VARCHAR(20) COMMENT 'PRODUCTION/CIRCULATION/SALES';

-- ============================================
-- 回滚脚本
-- ============================================
-- ALTER TABLE admin DROP COLUMN IF EXISTS agent_type;
