package org.virgil.nekokjs.mixin.mixins.worldgen;

import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.virgil.nekokjs.mixin.bridge.BridgeManager;
import org.virgil.nekokjs.mixin.cache.DensityCacheManager;

/**
 * DensityFunction Mixin
 * 用于拦截密度函数计算过程
 * 
 * 功能：
 * 1. 拦截 NoiseChunk 中的密度函数求值
 * 2. 允许 JavaScript 脚本注入自定义密度计算
 * 3. 提供保底方案，确保在 Folia 环境下密度函数可控
 * 
 * 使用场景：
 * - 自定义地形高度计算
 * - 修改洞穴生成密度
 * - 调整生物群系过渡
 * - 实现特殊地形效果（如浮空岛、反重力区域）
 * 
 * 性能优化：
 * - ✅ 使用独立缓存管理器（最多 50000 个值）
 * - ✅ 坐标哈希优化（精度到 0.1）
 * - ✅ 自动清理过期缓存
 * - ✅ 线程安全的并发访问
 * 
 * 注意：由于 DensityFunction 是接口，我们改为 Mixin NoiseChunk 类
 */
@Mixin(NoiseChunk.class)
public abstract class DensityFunctionMixin {
    
    /**
     * 拦截密度函数计算结果
     * 在 NoiseChunk 的内部类中修改 compute 返回值
     * 
     * @param value 原始密度值
     * @return 修改后的密度值
     */
    @ModifyVariable(
            method = "fillSlice",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/world/level/levelgen/DensityFunction;compute(Lnet/minecraft/world/level/levelgen/DensityFunction$FunctionContext;)D"
            ),
            ordinal = 0
    )
    private double nekokjs$modifyDensityValue(double value) {
        if (BridgeManager.INSTANCE.getBridge() == null) {
            return value;
        }
        
        try {
            // 获取当前 NoiseChunk 的坐标
            NoiseChunk self = (NoiseChunk) (Object) this;
            int x = self.blockX();
            int y = self.blockY();
            int z = self.blockZ();
            
            // 计算缓存键
            long cacheKey = DensityCacheManager.hashCoordinates(x, y, z);
            
            // 检查缓存
            Double cachedValue = DensityCacheManager.get(cacheKey);
            if (cachedValue != null) {
                DensityCacheManager.logStats();
                return cachedValue;
            }
            
            // 调用 JavaScript 钩子
            Double customDensity = BridgeManager.INSTANCE.getBridge()
                    .onDensityFunctionCompute(x, y, z, value);
            
            if (customDensity != null) {
                DensityCacheManager.put(cacheKey, customDensity);
                DensityCacheManager.logStats();
                return customDensity;
            } else {
                DensityCacheManager.put(cacheKey, value);
            }
        } catch (Exception e) {
            // 忽略错误，返回原值
        }
        
        DensityCacheManager.logStats();
        return value;
    }
}
