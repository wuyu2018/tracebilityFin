-- ============================================
-- 数据库迁移脚本：新建追溯链路中间表
-- 问题：溯源查询的原料、质检、仓储、运输数据分散在不同表，
--       且不同 API 路径写入不一致，导致后续录入的数据无法通过溯源码查到
-- 方案：统一通过 traceability_link 中间表串联批次与各环节实体
-- 日期：2026-05-23
-- ============================================

-- 1. 创建 traceability_link 表
CREATE TABLE IF NOT EXISTS traceability_link (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    batch_id    BIGINT       NOT NULL COMMENT '关联的 production_batch.id',
    entity_type VARCHAR(30)  NOT NULL COMMENT '实体类型: MATERIAL_PURCHASE / STORAGE / TRANSPORT_SALE / INSPECTION',
    entity_id   BIGINT       NOT NULL COMMENT '关联实体的主键 ID',
    created_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
    UNIQUE KEY uk_batch_entity (batch_id, entity_type, entity_id),
    INDEX       idx_tl_batch_id (batch_id),
    INDEX       idx_tl_entity (entity_type, entity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='追溯链路中间表';

-- 2. 从 batch_material_relation 迁移原料关系
INSERT IGNORE INTO traceability_link (batch_id, entity_type, entity_id, created_at)
SELECT id.batch_id, 'MATERIAL_PURCHASE', id.material_purchase_id, NOW(6)
FROM batch_material_relation;

-- 3. 从 storage 表迁移仓储记录
INSERT IGNORE INTO traceability_link (batch_id, entity_type, entity_id, created_at)
SELECT batch_id, 'STORAGE', id, COALESCE(storage_time, NOW(6))
FROM storage;

-- 4. 从 transport_sale 表迁移运输销售记录
INSERT IGNORE INTO traceability_link (batch_id, entity_type, entity_id, created_at)
SELECT batch_id, 'TRANSPORT_SALE', id, COALESCE(time, NOW(6))
FROM transport_sale;

-- 5. 从 inspection 表迁移质检记录
INSERT IGNORE INTO traceability_link (batch_id, entity_type, entity_id, created_at)
SELECT batch_id, 'INSPECTION', id, COALESCE(inspection_time, NOW(6))
FROM inspection;

-- ============================================
-- 回滚脚本
-- ============================================
-- DROP TABLE IF EXISTS traceability_link;
