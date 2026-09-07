-- 清理所有区块链数据，让系统重建
SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM offchain_storage;
DELETE FROM blockchain_log;
DELETE FROM block_header;
DELETE FROM consensus_state;
DELETE FROM blockchain_retry_task;
SET FOREIGN_KEY_CHECKS = 1;
