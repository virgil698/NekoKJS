package org.virgil.nekokjs.mixin.cache;

import net.minecraft.world.level.block.state.BlockState;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * 地表规则缓存管理器
 * 用于 SurfaceRulesMixin 的缓存
 */
public class SurfaceCacheManager {
    
    private static final int MAX_CACHE_SIZE = 20000;
    private static final Map<Long, BlockState> cache = new ConcurrentHashMap<>(MAX_CACHE_SIZE);
    
    private static long cacheHits = 0;
    private static long cacheMisses = 0;
    
    /**
     * 获取缓存值
     */
    public static BlockState get(long key) {
        BlockState value = cache.get(key);
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
    public static void put(long key, BlockState value) {
        // 检查缓存大小
        if (cache.size() >= MAX_CACHE_SIZE) {
            cache.clear();
            System.out.println("[NekoKJS] Surface cache cleared (reached max size)");
        }
        cache.put(key, value);
    }
    
    /**
     * 计算坐标哈希
     */
    public static long hashCoordinates(int x, int y, int z, int depth) {
        // 地表规则通常只关心 XZ 坐标和深度
        long hx = (long) (x / 16) * 16; // 区块级别精度
        long hz = (long) (z / 16) * 16;
        return hx * 73856093L ^ y * 19349663L ^ hz * 83492791L ^ depth * 12582917L;
    }
    
    /**
     * 输出缓存统计
     */
    public static void logStats() {
        long total = cacheHits + cacheMisses;
        if (total > 0 && total % 5000 == 0) {
            double hitRate = (double) cacheHits / total * 100;
            System.out.println(String.format(
                "[SurfaceRules] Cache stats - Hits: %d, Misses: %d, Hit rate: %.1f%%, Size: %d",
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
}
