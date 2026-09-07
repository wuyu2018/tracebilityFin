-- Consensus state persistence for PBFT
CREATE TABLE IF NOT EXISTS consensus_state (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sequence_number BIGINT NOT NULL,
    view VARCHAR(10) NOT NULL DEFAULT '0',
    digest VARCHAR(128),
    phase VARCHAR(20) NOT NULL DEFAULT 'ENDORSEMENT',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    prepare_count INT DEFAULT 0,
    commit_count INT DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_cs_seq_num (sequence_number),
    INDEX idx_cs_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
