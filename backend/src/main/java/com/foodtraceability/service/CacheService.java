package com.foodtraceability.service;

import com.foodtraceability.entity.BlockchainLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class CacheService {
    
    private static final Logger log = LoggerFactory.getLogger(CacheService.class);
    
    private static final String BLOOM_FILTER_KEY = "food:bloom";
    private static final String TRACEABILITY_PREFIX = "traceability:";
    private static final String BLOCKCHAIN_LOG_PREFIX = "blockchain:log:";
    private static final String AGENT_SESSION_PREFIX = "agent:session:";
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    
    public CacheService(RedisTemplate<String, Object> redisTemplate, 
                       StringRedisTemplate stringRedisTemplate) {
        this.redisTemplate = redisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
    }
    
    public void addFoodToBloomFilter(String foodId) {
        int hash = foodId.hashCode();
        redisTemplate.opsForValue().setBit(BLOOM_FILTER_KEY, hash, true);
    }
    
    public boolean mightContainFood(String foodId) {
        int hash = foodId.hashCode();
        Boolean exists = redisTemplate.opsForValue().getBit(BLOOM_FILTER_KEY, hash);
        return exists != null && exists;
    }
    
    public void cacheTraceabilityInfo(String foodId, Object traceabilityInfo) {
        String key = TRACEABILITY_PREFIX + foodId;
        redisTemplate.opsForValue().set(key, traceabilityInfo, 1, TimeUnit.HOURS);
    }
    
    public Object getTraceabilityInfo(String foodId) {
        String key = TRACEABILITY_PREFIX + foodId;
        return redisTemplate.opsForValue().get(key);
    }
    
    public void evictTraceabilityInfo(String foodId) {
        String key = TRACEABILITY_PREFIX + foodId;
        redisTemplate.delete(key);
    }
    
    public void cacheBlockchainLog(String entityType, Long entityId, List<BlockchainLog> logs) {
        String key = BLOCKCHAIN_LOG_PREFIX + entityType + ":" + entityId;
        redisTemplate.opsForValue().set(key, logs, 30, TimeUnit.MINUTES);
    }
    
    public List<BlockchainLog> getBlockchainLog(String entityType, Long entityId) {
        String key = BLOCKCHAIN_LOG_PREFIX + entityType + ":" + entityId;
        Object value = redisTemplate.opsForValue().get(key);
        if (value instanceof List) {
            return (List<BlockchainLog>) value;
        }
        return null;
    }
    
    public void evictBlockchainLog(String entityType, Long entityId) {
        String key = BLOCKCHAIN_LOG_PREFIX + entityType + ":" + entityId;
        redisTemplate.delete(key);
    }
    
    public void cacheAgentSession(String agentId, Object sessionData) {
        String key = AGENT_SESSION_PREFIX + agentId;
        redisTemplate.opsForValue().set(key, sessionData, 30, TimeUnit.MINUTES);
    }
    
    public Object getAgentSession(String agentId) {
        String key = AGENT_SESSION_PREFIX + agentId;
        return redisTemplate.opsForValue().get(key);
    }
    
    public void evictAgentSession(String agentId) {
        String key = AGENT_SESSION_PREFIX + agentId;
        redisTemplate.delete(key);
    }
    
    public void incrementCreditScore(String agentId, long delta) {
        String key = "agent:credit:" + agentId;
        redisTemplate.opsForValue().increment(key, delta);
    }
    
    public Long getCreditScore(String agentId) {
        String key = "agent:credit:" + agentId;
        Object value = redisTemplate.opsForValue().get(key);
        return value instanceof Number ? ((Number) value).longValue() : null;
    }
    
    public void publishConsensusMessage(String channel, Object message) {
        redisTemplate.convertAndSend(channel, message);
    }
}
