-- Blockchain retry task for failed on-chain appends
CREATE TABLE IF NOT EXISTS blockchain_retry_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chain_type VARCHAR(30) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    action VARCHAR(20) NOT NULL,
    raw_data TEXT NOT NULL,
    batch_id BIGINT,
    operator_id BIGINT,
    status VARCHAR(15) NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    max_retries INT NOT NULL DEFAULT 5,
    last_error TEXT,
    next_retry_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_brt_status (status),
    INDEX idx_brt_next_retry (next_retry_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
