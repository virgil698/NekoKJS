package org.virgil.nekokjs.mixin.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * 密度函数缓存管理器
 * 用于 DensityFunctionMixin 的缓存
 * 
 * 由于接口 Mixin 不能有非 Shadow 字段，所以使用独立的静态类
 */
public class DensityCacheManager {
    
    private static final int MAX_CACHE_SIZE = 50000;
    private static final Map<Long, Double> cache = new ConcurrentHashMap<>(MAX_CACHE_SIZE);
    
    private static long cacheHits = 0;
    private static long cacheMisses = 0;
    
    /**
     * 获取缓存值
     */
    public static Double get(long key) {
        Double value = cache.get(key);
        if (value != null) {
            cacheHits++;
        } else {
            cacheMisses++;
        }
        return value;
    }
    
    /**
     * 放入缓存
     */
    public static void put(long key, double value) {
        // 检查缓存大小
        if (cache.size() >= MAX_CACHE_SIZE) {
            cache.clear();
            System.out.println("[NekoKJS] Density cache cleared (reached max size)");
        }
        cache.put(key, value);
    }
    
    /**
     * 计算坐标哈希
     */
    public static long hashCoordinates(int x, int y, int z) {
        // 精度到 0.1（降低 10 倍）
        long hx = (long) (x / 10) * 10;
        long hy = (long) (y / 10) * 10;
        long hz = (long) (z / 10) * 10;
        
        // 使用质数混合哈希
        return hx * 73856093L ^ hy * 19349663L ^ hz * 83492791L;
    }
    
    /**
     * 输出缓存统计
     */
    public static void logStats() {
        long total = cacheHits + cacheMisses;
        if (total > 0 && total % 10000 == 0) {
            double hitRate = (double) cacheHits / total * 100;
            System.out.println(String.format(
                "[DensityFunction] Cache stats - Hits: %d, Misses: %d, Hit rate: %.1f%%, Size: %d",
                cacheHits, cacheMisses, hitRate, cache.size()
            ));
        }
    }
    
    /**
     * 清空缓存
     */
    public static void clear() {
        cache.clear();
        cacheHits = 0;
        cacheMisses = 0;
    }
    
    /**
     * 获取缓存大小
     */
    public static int size() {
        return cache.size();
    }
    
    /**
     * 获取统计信息
     */
    public static CacheStats getStats() {
        return new CacheStats(cache.size(), cacheHits, cacheMisses);
    }
    
    /**
     * 缓存统计信息
     */
    public static class CacheStats {
        public final int size;
        public final long hits;
        public final long misses;
        
        public CacheStats(int size, long hits, long misses) {
            this.size = size;
            this.hits = hits;
            this.misses = misses;
        }
        
        public double getHitRate() {
            long total = hits + misses;
            return total > 0 ? (double) hits / total * 100 : 0;
        }
    }
}
