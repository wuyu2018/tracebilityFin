package com.foodtraceability.security;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.BitSet;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
public class FoodBloomFilter {

    private static final String REDIS_KEY = "food:bloom:bitset";

    private final BitSet bitSet;
    private final int hashCount;
    private final int size;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    public FoodBloomFilter(int expectedElements, double falsePositiveRate) {
        this.size = (int) (-expectedElements * Math.log(falsePositiveRate)
                          / (Math.log(2) * Math.log(2)));
        this.hashCount = (int) (size / expectedElements * Math.log(2));
        this.bitSet = new BitSet(size);
    }

    public FoodBloomFilter() {
        this(100000, 0.01);
    }

    @PostConstruct
    public void init() {
        loadFromRedis();
    }

    public void add(String foodId) {
        for (int i = 0; i < hashCount; i++) {
            int hash = hash(foodId, i);
            bitSet.set(Math.abs(hash) % size);
        }
        saveToRedis();
    }

    public boolean mightContain(String foodId) {
        for (int i = 0; i < hashCount; i++) {
            int hash = hash(foodId, i);
            if (!bitSet.get(Math.abs(hash) % size)) {
                return false;
            }
        }
        return true;
    }

    private int hash(String foodId, int seed) {
        return Objects.hash(seed, foodId);
    }

    public byte[] toBytes() {
        return bitSet.toByteArray();
    }

    public void loadFromBytes(byte[] bytes) {
        bitSet.clear();
        bitSet.or(BitSet.valueOf(bytes));
    }

    public static FoodBloomFilter fromBytes(byte[] bytes, int expectedElements, double falsePositiveRate) {
        FoodBloomFilter filter = new FoodBloomFilter(expectedElements, falsePositiveRate);
        filter.loadFromBytes(bytes);
        return filter;
    }

    public int getSize() {
        return size;
    }

    public int getHashCount() {
        return hashCount;
    }

    private void saveToRedis() {
        if (redisTemplate == null) return;
        try {
            byte[] data = bitSet.toByteArray();
            redisTemplate.opsForValue().set(REDIS_KEY, data, 24, TimeUnit.HOURS);
        } catch (Exception e) {
            // Redis unavailable — continue with in-memory only
        }
    }

    public void loadFromRedis() {
        if (redisTemplate == null) return;
        try {
            Object value = redisTemplate.opsForValue().get(REDIS_KEY);
            if (value instanceof byte[] bytes) {
                loadFromBytes(bytes);
            }
        } catch (Exception e) {
            // Redis unavailable — start with empty filter
        }
    }
}
