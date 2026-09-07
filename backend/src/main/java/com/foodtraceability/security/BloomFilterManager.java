package com.foodtraceability.security;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BloomFilterManager {

    private final Map<String, FoodBloomFilter> filters = new ConcurrentHashMap<>();

    public void add(String chainType, String foodId) {
        filters.computeIfAbsent(chainType, k -> new FoodBloomFilter()).add(foodId);
    }

    public boolean mightContain(String chainType, String foodId) {
        FoodBloomFilter filter = filters.get(chainType);
        return filter != null && filter.mightContain(foodId);
    }

    public byte[] toBytes(String chainType) {
        return filters.computeIfAbsent(chainType, k -> new FoodBloomFilter()).toBytes();
    }

    public void loadFromBytes(String chainType, byte[] bytes) {
        FoodBloomFilter filter = new FoodBloomFilter();
        filter.loadFromBytes(bytes);
        filters.put(chainType, filter);
    }

    public int getSize(String chainType) {
        FoodBloomFilter f = filters.get(chainType);
        return f != null ? f.getSize() : 0;
    }
}
