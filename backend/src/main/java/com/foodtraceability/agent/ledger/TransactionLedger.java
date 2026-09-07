package com.foodtraceability.agent.ledger;

import com.foodtraceability.agent.core.TransactionRecord;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Optional;
import java.time.Instant;
import java.util.List;
import java.util.ArrayList;

@Repository
public class TransactionLedger {
    
    private final Map<String, TransactionRecord> transactions = new ConcurrentHashMap<>();
    private final Map<String, Map<String, TransactionRecord>> agentTransactions = new ConcurrentHashMap<>();
    private final Map<String, AgentReputation> reputationRecords = new ConcurrentHashMap<>();
    
    public void addTransaction(TransactionRecord transaction) {
        transactions.put(transaction.getTransactionId(), transaction);
        
        agentTransactions
            .computeIfAbsent(transaction.getRequesterAgentId(), k -> new ConcurrentHashMap<>())
            .put(transaction.getTransactionId(), transaction);
        
        agentTransactions
            .computeIfAbsent(transaction.getProviderAgentId(), k -> new ConcurrentHashMap<>())
            .put(transaction.getTransactionId(), transaction);
        
        initializeReputationIfNotExists(transaction.getRequesterAgentId());
        initializeReputationIfNotExists(transaction.getProviderAgentId());
    }
    
    private void initializeReputationIfNotExists(String agentId) {
        reputationRecords.putIfAbsent(agentId, new AgentReputation(agentId));
    }
    
    public Optional<TransactionRecord> getTransaction(String transactionId) {
        return Optional.ofNullable(transactions.get(transactionId));
    }
    
    public List<TransactionRecord> findTransactionByAgent(String agentId) {
        Map<String, TransactionRecord> agentTxns = agentTransactions.get(agentId);
        if (agentTxns == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(agentTxns.values());
    }
    
    public List<TransactionRecord> findTransactionByTimeRange(String agentId, Instant start, Instant end) {
        return findTransactionByAgent(agentId).stream()
            .filter(t -> !t.getTimestamp().isBefore(start) && !t.getTimestamp().isAfter(end))
            .toList();
    }
    
    public void commitTransaction(String transactionId) {
        TransactionRecord txn = transactions.get(transactionId);
        if (txn != null) {
            txn.setStatus(TransactionRecord.TransactionStatus.COMMITTED);
            
            if (txn.getCreditChange() != 0) {
                updateReputation(txn.getRequesterAgentId(), txn.getCreditChange());
                updateReputation(txn.getProviderAgentId(), txn.getCreditChange());
            }
        }
    }
    
    public void updateReputation(String agentId, long delta) {
        initializeReputationIfNotExists(agentId);
        AgentReputation reputation = reputationRecords.get(agentId);
        reputation.addScore(delta);
    }
    
    public long calculateReputation(String agentId) {
        AgentReputation reputation = reputationRecords.get(agentId);
        return reputation != null ? reputation.getTotalScore() : 0;
    }
    
    public AgentReputation getReputationRecord(String agentId) {
        return reputationRecords.get(agentId);
    }
    
    public Iterable<AgentReputation> getAllReputations() {
        return reputationRecords.values();
    }
}
