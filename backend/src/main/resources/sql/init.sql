-- =======================================================
-- 1. 初始化数据库（默认 utf8mb4 字符集）
-- =======================================================
CREATE DATABASE IF NOT EXISTS `food_traceability` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE IF NOT EXISTS `food_traceability_anchor` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- =======================================================
-- 2. 主数据库表结构 (food_traceability)
-- =======================================================
USE `food_traceability`;

-- 表1：company（公司信息表）
CREATE TABLE `company` (
                           `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '公司ID',
                           `name` VARCHAR(100) NOT NULL COMMENT '公司名称',
                           `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
                           `address` VARCHAR(255) DEFAULT NULL COMMENT '地址',
                           `created_at` DATETIME NOT NULL COMMENT '创建时间',
                           `updated_at` DATETIME NOT NULL COMMENT '更新时间',
                           PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公司信息表';

-- 表2：admin（管理员/用户表）
CREATE TABLE `admin` (
                         `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                         `username` VARCHAR(50) NOT NULL COMMENT '用户名（4-20位字母数字）',
                         `password` VARCHAR(100) NOT NULL COMMENT 'BCrypt加密密码（含大小写字母+数字+特殊字符）',
                         `role` VARCHAR(20) DEFAULT 'ADMIN' COMMENT '角色（ADMIN/SUPER_ADMIN）',
                         `agent_type` VARCHAR(20) DEFAULT NULL COMMENT '智能体类型（PRODUCTION/CIRCULATION/SALES/null）',
                         `company_id` BIGINT DEFAULT NULL COMMENT '所属公司ID',
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `uk_admin_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员/用户表';

-- 表3：product（产品表）
CREATE TABLE `product` (
                           `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '产品ID',
                           `name` VARCHAR(100) NOT NULL COMMENT '产品名称',
                           `specification` VARCHAR(50) DEFAULT NULL COMMENT '规格型号',
                           `shelf_life` VARCHAR(50) DEFAULT NULL COMMENT '保质期',
                           `image_url` VARCHAR(500) DEFAULT NULL COMMENT '产品图片URL',
                           `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
                           `contact_email` VARCHAR(100) DEFAULT NULL COMMENT '联系邮箱',
                           `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除标记',
                           `company_id` BIGINT DEFAULT NULL COMMENT '所属公司ID',
                           PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品表';

-- 表4：material（原料品种表）
CREATE TABLE `material` (
                            `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '原料ID',
                            `name` VARCHAR(100) NOT NULL COMMENT '原料名称',
                            `is_active` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
                            `created_at` DATETIME NOT NULL COMMENT '创建时间',
                            `updated_at` DATETIME NOT NULL COMMENT '更新时间',
                            `company_id` BIGINT DEFAULT NULL COMMENT '所属公司ID',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_material_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='原料品种表';

-- 表5：material_purchase（原料采购表）
CREATE TABLE `material_purchase` (
                                     `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '采购ID',
                                     `material_id` BIGINT NOT NULL COMMENT '关联原料品种',
                                     `batch_number` VARCHAR(50) DEFAULT NULL COMMENT '原料批次号',
                                     `supplier_name` VARCHAR(100) DEFAULT NULL COMMENT '供应商名称',
                                     `producer_name` VARCHAR(100) DEFAULT NULL COMMENT '生产商名称',
                                     `producer_address` VARCHAR(255) DEFAULT NULL COMMENT '生产商地址',
                                     `purchase_date` DATETIME DEFAULT NULL COMMENT '采购日期',
                                     `quantity` DOUBLE DEFAULT NULL COMMENT '采购数量',
                                     `unit` VARCHAR(20) DEFAULT NULL COMMENT '单位',
                                     `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除标记',
                                     `company_id` BIGINT DEFAULT NULL COMMENT '所属公司ID',
                                     PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='原料采购表';

-- 表6：production_batch（生产批次表）
CREATE TABLE `production_batch` (
                                    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '批次ID',
                                    `batch_number` VARCHAR(50) NOT NULL COMMENT '批次号',
                                    `product_id` BIGINT NOT NULL COMMENT '关联产品',
                                    `production_date` DATE NOT NULL COMMENT '生产日期',
                                    `shelf_life` VARCHAR(50) DEFAULT NULL COMMENT '保质期',
                                    `quantity` DOUBLE DEFAULT NULL COMMENT '数量',
                                    `unit` VARCHAR(20) DEFAULT NULL COMMENT '单位',
                                    `storage_id` BIGINT DEFAULT NULL COMMENT '关联仓储记录ID',
                                    `transport_sale_id` BIGINT DEFAULT NULL COMMENT '关联运输销售记录ID',
                                    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除标记',
                                    `created_at` DATETIME DEFAULT NULL COMMENT '创建时间',
                                    `company_id` BIGINT DEFAULT NULL COMMENT '所属公司ID',
                                    PRIMARY KEY (`id`),
                                    UNIQUE KEY `uk_prod_batch` (`product_id`, `batch_number`) COMMENT '批次唯一约束'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='生产批次表';

-- 表7：batch_material_relation（批次-原料关联表）
CREATE TABLE `batch_material_relation` (
                                           `batch_id` BIGINT NOT NULL COMMENT '批次ID（复合主键）',
                                           `material_purchase_id` BIGINT NOT NULL COMMENT '原料采购ID（复合主键）',
                                           PRIMARY KEY (`batch_id`, `material_purchase_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='批次-原料关联表';

-- 表8：inspection（检验记录表）
CREATE TABLE `inspection` (
                              `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '检验ID',
                              `batch_id` BIGINT NOT NULL COMMENT '关联批次',
                              `sample_name` VARCHAR(100) DEFAULT NULL COMMENT '样品名称',
                              `sample_quantity` INT DEFAULT NULL COMMENT '样品数量',
                              `sample_specification` VARCHAR(100) DEFAULT NULL COMMENT '样品规格',
                              `image_url` VARCHAR(500) DEFAULT NULL COMMENT '检验图片URL',
                              `result_status` VARCHAR(20) DEFAULT NULL COMMENT '检验结果（合格/不合格）',
                              `result_detail` VARCHAR(500) DEFAULT NULL COMMENT '检验详情',
                              `inspector_name` VARCHAR(50) DEFAULT NULL COMMENT '检验员',
                              `inspection_time` DATETIME DEFAULT NULL COMMENT '检验时间',
                              `company_id` BIGINT DEFAULT NULL COMMENT '所属公司ID',
                              PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检验记录表';

-- 表9：storage（仓储记录表）
CREATE TABLE `storage` (
                           `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '仓储ID',
                           `batch_id` BIGINT NOT NULL COMMENT '关联批次',
                           `storage_time` DATETIME DEFAULT NULL COMMENT '入库时间',
                           `outbound_time` DATETIME DEFAULT NULL COMMENT '出库时间',
                           `quantity` DOUBLE DEFAULT NULL COMMENT '数量',
                           `unit` VARCHAR(20) DEFAULT NULL COMMENT '单位',
                           `warehouse_location` VARCHAR(100) DEFAULT NULL COMMENT '仓库位置',
                           `company_id` BIGINT DEFAULT NULL COMMENT '所属公司ID',
                           PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓储记录表';

-- 表10：transport_sale（运输销售记录表）
CREATE TABLE `transport_sale` (
                                  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
                                  `batch_id` BIGINT DEFAULT NULL COMMENT '关联批次',
                                  `environment_temperature` DOUBLE DEFAULT NULL COMMENT '环境温度',
                                  `product_temperature` DOUBLE DEFAULT NULL COMMENT '产品温度',
                                  `time` DATETIME DEFAULT NULL COMMENT '记录时间',
                                  `transport_company` VARCHAR(100) DEFAULT NULL COMMENT '运输公司',
                                  `vehicle_number` VARCHAR(50) DEFAULT NULL COMMENT '车牌号',
                                  `sales_region` VARCHAR(255) DEFAULT NULL COMMENT '销售区域',
                                  `receiver_name` VARCHAR(100) DEFAULT NULL COMMENT '收货人',
                                  `receiver_contact` VARCHAR(50) DEFAULT NULL COMMENT '收货人联系方式',
                                  `recorder_name` VARCHAR(50) DEFAULT NULL COMMENT '记录人',
                                  `company_id` BIGINT DEFAULT NULL COMMENT '所属公司ID',
                                  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运输销售记录表';

-- 表11：security_code（安全码表）
CREATE TABLE `security_code` (
                                 `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
                                 `code` VARCHAR(64) NOT NULL COMMENT '安全码（格式：SC + 时间戳 + 随机数 + UUID片段）',
                                 `batch_id` BIGINT NOT NULL COMMENT '关联批次',
                                 `status` VARCHAR(20) NOT NULL COMMENT '状态（未激活/已激活/已冻结）',
                                 `first_scan_time` DATETIME DEFAULT NULL COMMENT '首次扫描时间',
                                 `scan_count` INT DEFAULT 0 COMMENT '查询次数',
                                 `created_at` DATETIME DEFAULT NULL COMMENT '创建时间',
                                 `company_id` BIGINT DEFAULT NULL COMMENT '所属公司ID',
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `uk_security_code` (`code`),
                                 INDEX `idx_security_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='安全码表';

-- 表12：complaint（投诉表）
CREATE TABLE `complaint` (
                             `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '投诉ID',
                             `security_code_id` BIGINT DEFAULT NULL COMMENT '关联安全码',
                             `complaint_reason` VARCHAR(500) DEFAULT NULL COMMENT '投诉原因',
                             `complaint_time` DATETIME DEFAULT NULL COMMENT '投诉时间',
                             PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投诉表';

-- 表13：blockchain_log（区块链操作日志表）
CREATE TABLE `blockchain_log` (
                                  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
                                  `block_header_id` BIGINT DEFAULT NULL COMMENT '关联区块头',
                                  `batch_id` BIGINT DEFAULT NULL COMMENT '关联批次ID',
                                  `chain_type` VARCHAR(30) NOT NULL COMMENT '链类型（MATERIAL/BATCH）',
                                  `entity_type` VARCHAR(50) NOT NULL COMMENT '实体类型（PRODUCT/MATERIAL/INSPECTION等）',
                                  `entity_id` BIGINT NOT NULL COMMENT '实体ID',
                                  `action` VARCHAR(20) NOT NULL COMMENT '操作类型（CREATE/UPDATE/DELETE）',
                                  `previous_hash` VARCHAR(128) DEFAULT NULL COMMENT '前一个区块哈希',
                                  `current_hash` VARCHAR(128) NOT NULL COMMENT '当前区块哈希（SHA-256）',
                                  `data_snapshot` TEXT COMMENT '数据快照JSON（遗留字段，已废弃）',
                                  `data_hash` VARCHAR(64) DEFAULT NULL COMMENT '链下存储数据哈希（SHA-256）',
                                  `offchain_ref` VARCHAR(200) DEFAULT NULL COMMENT '链下存储引用（对应offchain_storage.food_id）',
                                  `signature` VARCHAR(512) NOT NULL COMMENT 'RSA-2048签名',
                                  `timestamp` DATETIME NOT NULL COMMENT '时间戳',
                                  `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
                                  `ref_master_chain_hash` VARCHAR(128) DEFAULT NULL COMMENT '主链交叉引用哈希',
                                  PRIMARY KEY (`id`),
                                  INDEX `idx_bl_batch_id` (`batch_id`),
                                  INDEX `idx_bl_timestamp` (`timestamp`),
                                  INDEX `idx_bl_chain_type` (`chain_type`),
                                  INDEX `idx_bl_entity` (`entity_type`, `entity_id`),
                                  INDEX `idx_bl_data_hash` (`data_hash`),
                                  INDEX `idx_bl_traceability` (`chain_type`, `entity_type`, `entity_id`, `timestamp`, `current_hash`, `data_hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='区块链操作日志表';

-- 表14：block_header（区块头信息表）
CREATE TABLE `block_header` (
                                `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
                                `chain_type` VARCHAR(30) NOT NULL COMMENT '链类型（MATERIAL/BATCH）',
                                `block_hash` VARCHAR(128) NOT NULL COMMENT '区块哈希（SHA-256(chainType+prevHash+merkleRoot+timestamp+txCount)）',
                                `previous_hash` VARCHAR(128) DEFAULT NULL COMMENT '前一个区块哈希',
                                `merkle_root` VARCHAR(128) DEFAULT NULL COMMENT 'Merkle树根哈希',
                                `timestamp` DATETIME NOT NULL COMMENT '时间戳',
                                `bloom_filter` BLOB DEFAULT NULL COMMENT '合并布隆过滤器二进制数据（压缩存储整块交易）',
                                `metadata_index` JSON DEFAULT NULL COMMENT '本块元数据索引（JSON格式，记录块内交易摘要）',
                                `tx_count` INT DEFAULT NULL COMMENT '包含交易数',
                                PRIMARY KEY (`id`),
                                INDEX `idx_bh_chain_type` (`chain_type`),
                                INDEX `idx_bh_timestamp` (`timestamp`),
                                INDEX `idx_bh_block_hash` (`block_hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='区块头信息表';

-- 表15：offchain_storage（链下存储表）
CREATE TABLE `offchain_storage` (
                                    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
                                    `food_id` VARCHAR(100) NOT NULL COMMENT '食品/数据唯一标识',
                                    `data_hash` VARCHAR(64) NOT NULL COMMENT '数据哈希（SHA-256）',
                                    `storage_type` VARCHAR(20) NOT NULL COMMENT '存储类型（REDIS/LOCAL_FILE/OSS/DATABASE）',
                                    `storage_key` VARCHAR(200) NOT NULL COMMENT '存储键',
                                    `encryption_method` VARCHAR(50) DEFAULT NULL COMMENT '加密方法（AES-256-GCM等）',
                                    `encrypted_data` TEXT DEFAULT NULL COMMENT 'AES-256-GCM加密的数据',
                                    `encrypted_aes_key` TEXT DEFAULT NULL COMMENT 'RSA加密的AES密钥',
                                    `owner_agent_id` BIGINT NOT NULL COMMENT '所属智能体ID',
                                    `access_policy` JSON DEFAULT NULL COMMENT '访问控制策略JSON',
                                    `created_at` DATETIME NOT NULL COMMENT '创建时间',
                                    `expires_at` DATETIME DEFAULT NULL COMMENT '过期时间',
                                    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除标记',
                                    PRIMARY KEY (`id`),
                                    UNIQUE KEY `uk_offchain_food_id` (`food_id`),
                                    INDEX `idx_offchain_food_id` (`food_id`),
                                    INDEX `idx_offchain_storage` (`storage_type`, `storage_key`),
                                    INDEX `idx_offchain_owner` (`owner_agent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='链下存储表';

-- 表16：traceability_link（溯源链路表）
CREATE TABLE `traceability_link` (
                                     `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
                                     `batch_id` BIGINT NOT NULL COMMENT '关联批次ID',
                                     `entity_type` VARCHAR(30) NOT NULL COMMENT '实体类型（MATERIAL_PURCHASE/STORAGE/TRANSPORT_SALE/INSPECTION）',
                                     `entity_id` BIGINT NOT NULL COMMENT '实体ID',
                                     `created_at` DATETIME(6) NOT NULL COMMENT '创建时间（微秒级精度）',
                                     PRIMARY KEY (`id`),
                                     UNIQUE KEY `uk_batch_entity` (`batch_id`, `entity_type`, `entity_id`),
                                     INDEX `idx_tl_batch_id` (`batch_id`),
                                     INDEX `idx_tl_entity` (`entity_type`, `entity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='溯源链路表';

-- 表17：consensus_state（PBFT共识状态表）
CREATE TABLE `consensus_state` (
                                   `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
                                   `sequence_number` BIGINT NOT NULL COMMENT '共识序列号',
                                   `view` VARCHAR(10) NOT NULL COMMENT '视图编号',
                                   `digest` VARCHAR(128) DEFAULT NULL COMMENT '消息摘要',
                                   `phase` VARCHAR(20) NOT NULL COMMENT '共识阶段（ENDORSEMENT/PRE_PREPARE/PREPARE/COMMIT/EXECUTED）',
                                   `status` VARCHAR(20) NOT NULL COMMENT '共识状态（PENDING/ACCEPTED/REJECTED/COMMITTED）',
                                   `prepare_count` INT DEFAULT 0 COMMENT 'Prepare消息数',
                                   `commit_count` INT DEFAULT 0 COMMENT 'Commit消息数',
                                   `created_at` DATETIME NOT NULL COMMENT '创建时间',
                                   `updated_at` DATETIME NOT NULL COMMENT '更新时间',
                                   PRIMARY KEY (`id`),
                                   INDEX `idx_cs_seq_num` (`sequence_number`),
                                   INDEX `idx_cs_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='PBFT共识状态表';

-- 表18：blockchain_retry_task（上链重试任务表）
CREATE TABLE `blockchain_retry_task` (
                                         `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '任务ID',
                                         `chain_type` VARCHAR(30) NOT NULL COMMENT '链类型',
                                         `entity_type` VARCHAR(50) NOT NULL COMMENT '实体类型',
                                         `entity_id` BIGINT NOT NULL COMMENT '实体ID',
                                         `action` VARCHAR(20) NOT NULL COMMENT '操作类型',
                                         `raw_data` TEXT NOT NULL COMMENT '原始数据JSON',
                                         `batch_id` BIGINT DEFAULT NULL COMMENT '关联批次ID',
                                         `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
                                         `status` VARCHAR(15) NOT NULL COMMENT '状态（PENDING/PROCESSING/SUCCESS/FAILED）',
                                         `retry_count` INT NOT NULL DEFAULT 0 COMMENT '已重试次数',
                                         `max_retries` INT NOT NULL DEFAULT 5 COMMENT '最大重试次数',
                                         `last_error` TEXT DEFAULT NULL COMMENT '最后错误信息',
                                         `next_retry_at` DATETIME NOT NULL COMMENT '下次重试时间（指数退避）',
                                         `created_at` DATETIME NOT NULL COMMENT '创建时间',
                                         `updated_at` DATETIME NOT NULL COMMENT '更新时间',
                                         PRIMARY KEY (`id`),
                                         INDEX `idx_brt_status` (`status`),
                                         INDEX `idx_brt_next_retry` (`next_retry_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='上链重试任务表';

-- 表19：smart_contract_state（智能合约状态表）
CREATE TABLE `smart_contract_state` (
                                        `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
                                        `contract_id` VARCHAR(50) NOT NULL COMMENT '合约ID',
                                        `contract_type` VARCHAR(50) NOT NULL COMMENT '合约类型（PERMISSION_CONTROL/TRANSACTION_OPERATION/DATA_ONCHAIN/STATE_UPDATE）',
                                        `state_key` VARCHAR(200) NOT NULL COMMENT '状态键',
                                        `state_value` TEXT DEFAULT NULL COMMENT '状态值',
                                        `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号（Java层初始化为1）',
                                        `updated_at` DATETIME NOT NULL COMMENT '更新时间',
                                        PRIMARY KEY (`id`),
                                        UNIQUE KEY `uk_contract_state` (`contract_id`, `state_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能合约状态表';

-- 表20：agent_identity（智能体身份表）
CREATE TABLE `agent_identity` (
                                  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
                                  `agent_id` VARCHAR(50) NOT NULL COMMENT '智能体ID',
                                  `agent_type` VARCHAR(30) NOT NULL COMMENT '智能体类型（PRODUCTION/CIRCULATION/SALES/CERTIFICATE_AUTHORITY等）',
                                  `certificate_serial` VARCHAR(100) DEFAULT NULL COMMENT '证书序列号',
                                  `public_key` TEXT DEFAULT NULL COMMENT 'RSA公钥',
                                  `credit_score` BIGINT NOT NULL DEFAULT 100 COMMENT '信誉积分',
                                  `status` VARCHAR(20) NOT NULL COMMENT '状态（REGISTERED/CERTIFIED/ACTIVE/SUSPENDED/REVOKED）',
                                  `registered_at` DATETIME NOT NULL COMMENT '注册时间',
                                  `last_active_at` DATETIME DEFAULT NULL COMMENT '最后活跃时间',
                                  `metadata` JSON DEFAULT NULL COMMENT '元数据JSON',
                                  PRIMARY KEY (`id`),
                                  UNIQUE KEY `uk_agent_id` (`agent_id`),
                                  INDEX `idx_agent_type` (`agent_type`),
                                  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能体身份表';


-- =======================================================
-- 3. 锚点数据库表结构 (food_traceability_anchor)
-- =======================================================
USE `food_traceability_anchor`;

-- 表21：blockchain_anchor（区块链锚点表）
CREATE TABLE `blockchain_anchor` (
                                     `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
                                     `chain_type` VARCHAR(30) NOT NULL COMMENT '链类型（MATERIAL/BATCH）',
                                     `batch_id` BIGINT DEFAULT NULL COMMENT '批次ID（可为空，表示全局锚点）',
                                     `current_hash` VARCHAR(128) NOT NULL COMMENT '锚定时刻的当前链最新哈希',
                                     `anchor_date` DATE NOT NULL COMMENT '锚定日期（按日锚定）',
                                     `created_at` DATETIME NOT NULL COMMENT '创建时间',
                                     PRIMARY KEY (`id`),
                                     INDEX `idx_ba_batch_date` (`batch_id`, `anchor_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='区块链锚点表';

-- 表22：operation_log（操作审计日志表）
CREATE TABLE `operation_log` (
                                 `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
                                 `operator` VARCHAR(50) DEFAULT NULL COMMENT '操作人',
                                 `entity_type` VARCHAR(50) NOT NULL COMMENT '操作实体类型',
                                 `entity_id` BIGINT DEFAULT NULL COMMENT '操作实体ID',
                                 `action` VARCHAR(20) NOT NULL COMMENT '操作类型',
                                 `summary` VARCHAR(500) DEFAULT NULL COMMENT '操作摘要',
                                 `created_at` DATETIME NOT NULL COMMENT '操作时间',
                                 PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作审计日志表';


-- =======================================================
-- 4. 统一添加外键关联约束 (防止建表时的循环依赖问题)
-- =======================================================
USE `food_traceability`;

-- 关联 company 表的外键
ALTER TABLE `admin` ADD CONSTRAINT `fk_admin_company` FOREIGN KEY (`company_id`) REFERENCES `company` (`id`);
ALTER TABLE `product` ADD CONSTRAINT `fk_product_company` FOREIGN KEY (`company_id`) REFERENCES `company` (`id`);
ALTER TABLE `material` ADD CONSTRAINT `fk_material_company` FOREIGN KEY (`company_id`) REFERENCES `company` (`id`);
ALTER TABLE `material_purchase` ADD CONSTRAINT `fk_mp_company` FOREIGN KEY (`company_id`) REFERENCES `company` (`id`);
ALTER TABLE `production_batch` ADD CONSTRAINT `fk_pb_company` FOREIGN KEY (`company_id`) REFERENCES `company` (`id`);
ALTER TABLE `inspection` ADD CONSTRAINT `fk_ins_company` FOREIGN KEY (`company_id`) REFERENCES `company` (`id`);
ALTER TABLE `storage` ADD CONSTRAINT `fk_storage_company` FOREIGN KEY (`company_id`) REFERENCES `company` (`id`);
ALTER TABLE `transport_sale` ADD CONSTRAINT `fk_ts_company` FOREIGN KEY (`company_id`) REFERENCES `company` (`id`);
ALTER TABLE `security_code` ADD CONSTRAINT `fk_sc_company` FOREIGN KEY (`company_id`) REFERENCES `company` (`id`);

-- 关联其他业务主表的外键
ALTER TABLE `material_purchase` ADD CONSTRAINT `fk_mp_material` FOREIGN KEY (`material_id`) REFERENCES `material` (`id`);
ALTER TABLE `production_batch` ADD CONSTRAINT `fk_pb_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`);
ALTER TABLE `inspection` ADD CONSTRAINT `fk_ins_batch` FOREIGN KEY (`batch_id`) REFERENCES `production_batch` (`id`);
ALTER TABLE `storage` ADD CONSTRAINT `fk_storage_batch` FOREIGN KEY (`batch_id`) REFERENCES `production_batch` (`id`);
ALTER TABLE `transport_sale` ADD CONSTRAINT `fk_ts_batch` FOREIGN KEY (`batch_id`) REFERENCES `production_batch` (`id`);
ALTER TABLE `security_code` ADD CONSTRAINT `fk_sc_batch` FOREIGN KEY (`batch_id`) REFERENCES `production_batch` (`id`);
ALTER TABLE `complaint` ADD CONSTRAINT `fk_complaint_sc` FOREIGN KEY (`security_code_id`) REFERENCES `security_code` (`id`);

-- 关联区块链相关的外键
ALTER TABLE `blockchain_log` ADD CONSTRAINT `fk_bl_header` FOREIGN KEY (`block_header_id`) REFERENCES `block_header` (`id`);
ALTER TABLE `blockchain_log` ADD CONSTRAINT `fk_bl_batch` FOREIGN KEY (`batch_id`) REFERENCES `production_batch` (`id`);
ALTER TABLE `batch_material_relation` ADD CONSTRAINT `fk_bmr_batch` FOREIGN KEY (`batch_id`) REFERENCES `production_batch` (`id`);
ALTER TABLE `batch_material_relation` ADD CONSTRAINT `fk_bmr_mp` FOREIGN KEY (`material_purchase_id`) REFERENCES `material_purchase` (`id`);