package org.virgil.nekokjs.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 缓存管理器
 * 用于高频 Mixin 调用的结果缓存
 * 
 * 特性：
 * - LRU 缓存策略
 * - 过期时间控制
 * - 线程安全
 * - 自动清理
 */
public class CacheManager<K, V> {
    private static final Logger LOGGER = Logger.getLogger("NekoKJS-Cache");
    
    private final Map<K, CacheEntry<V>> cache;
    private final int maxSize;
    private final long ttlMillis;
    
    // 性能统计
    private long hits = 0;
    private long misses = 0;
    private long evictions = 0;
    
    public CacheManager(int maxSize, long ttlMillis) {
        this.cache = new ConcurrentHashMap<>(maxSize);
        this.maxSize = maxSize;
        this.ttlMillis = ttlMillis;
    }
    
    /**
     * 获取缓存值
     */
    public V get(K key) {
        CacheEntry<V> entry = cache.get(key);
        
        if (entry == null) {
            misses++;
            return null;
        }
        
        // 检查是否过期
        if (isExpired(entry)) {
            cache.remove(key);
            misses++;
            return null;
        }
        
        hits++;
        entry.lastAccessTime = System.currentTimeMillis();
        return entry.value;
    }
    
    /**
     * 放入缓存
     */
    public void put(K key, V value) {
        // 检查缓存大小
        if (cache.size() >= maxSize) {
            evictOldest();
        }
        
        CacheEntry<V> entry = new CacheEntry<>(value);
        cache.put(key, entry);
    }
    
    /**
     * 获取或计算
     */
    public V getOrCompute(K key, java.util.function.Supplier<V> supplier) {
        V cached = get(key);
        if (cached != null) {
            return cached;
        }
        
        V computed = supplier.get();
        if (computed != null) {
            put(key, computed);
        }
        return computed;
    }
    
    /**
     * 清除缓存
     */
    public void clear() {
        cache.clear();
        hits = 0;
        misses = 0;
        evictions = 0;
    }
    
    /**
     * 清除过期条目
     */
    public void cleanExpired() {
        cache.entrySet().removeIf(entry -> isExpired(entry.getValue()));
    }
    
    /**
     * 检查是否过期
     */
    private boolean isExpired(CacheEntry<V> entry) {
        if (ttlMillis <= 0) return false;
        return System.currentTimeMillis() - entry.createTime > ttlMillis;
    }
    
    /**
     * 驱逐最旧的条目
     */
    private void evictOldest() {
        if (cache.isEmpty()) return;
        
        K oldestKey = null;
        long oldestTime = Long.MAX_VALUE;
        
        for (Map.Entry<K, CacheEntry<V>> entry : cache.entrySet()) {
            if (entry.getValue().lastAccessTime < oldestTime) {
                oldestTime = entry.getValue().lastAccessTime;
                oldestKey = entry.getKey();
            }
        }
        
        if (oldestKey != null) {
            cache.remove(oldestKey);
            evictions++;
        }
    }
    
    /**
     * 获取缓存统计
     */
    public CacheStats getStats() {
        return new CacheStats(cache.size(), hits, misses, evictions);
    }
    
    /**
     * 缓存条目
     */
    private static class CacheEntry<V> {
        final V value;
        final long createTime;
        long lastAccessTime;
        
        CacheEntry(V value) {
            this.value = value;
            this.createTime = System.currentTimeMillis();
            this.lastAccessTime = createTime;
        }
    }
    
    /**
     * 缓存统计
     */
    public static class CacheStats {
        public final int size;
        public final long hits;
        public final long misses;
        public final long evictions;
        
        public CacheStats(int size, long hits, long misses, long evictions) {
            this.size = size;
            this.hits = hits;
            this.misses = misses;
            this.evictions = evictions;
        }
        
        public double getHitRate() {
            long total = hits + misses;
            return total > 0 ? (double) hits / total * 100 : 0;
        }
        
        @Override
        public String toString() {
            return String.format(
                "CacheStats{size=%d, hits=%d, misses=%d, hitRate=%.1f%%, evictions=%d}",
                size, hits, misses, getHitRate(), evictions
            );
        }
    }
}
