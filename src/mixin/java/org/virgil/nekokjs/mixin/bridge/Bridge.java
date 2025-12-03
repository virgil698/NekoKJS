package org.virgil.nekokjs.mixin.bridge;

import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.RandomState;

import java.util.concurrent.CompletableFuture;

/**
 * Bridge 接口，用于 Mixin 模块和主插件模块之间的通信
 * Mixin 模块无法直接访问主插件类，需要通过 Bridge 进行交互
 */
public interface Bridge {
    /**
     * 服务器启动完成时调用
     */
    void onServerStarted();

    /**
     * 服务器每 tick 调用
     */
    void onServerTick();

    /**
     * 获取插件实例
     */
    Object getPlugin();
    
    // ===== 世界生成相关方法 =====
    
    /**
     * 区块地表生成时调用
     * @param level 世界生成区域
     * @param chunk 区块访问器
     * @return true 表示脚本已处理，取消原版逻辑；false 表示使用原版逻辑
     */
    boolean onChunkSurfaceGenerate(WorldGenRegion level, ChunkAccess chunk);
    
    /**
     * 区块噪声填充时调用
     * @param chunk 区块访问器
     * @param randomState 随机状态
     * @return 如果返回非 null，使用脚本提供的结果；否则使用原版逻辑
     */
    CompletableFuture<ChunkAccess> onChunkNoiseGenerate(ChunkAccess chunk, RandomState randomState);
    
    /**
     * 区块洞穴雕刻时调用
     * @param level 世界生成区域
     * @param chunk 区块访问器
     * @param seed 种子
     * @return true 表示脚本已处理，取消原版逻辑；false 表示使用原版逻辑
     */
    boolean onChunkCarverGenerate(WorldGenRegion level, ChunkAccess chunk, long seed);
    
    /**
     * 生物群系选择时调用
     * @param x 四分之一方块 X 坐标
     * @param y 四分之一方块 Y 坐标
     * @param z 四分之一方块 Z 坐标
     * @param sampler 气候采样器
     * @return 如果返回非 null，使用脚本提供的生物群系；否则使用原版逻辑
     */
    Holder<Biome> onBiomeSelect(int x, int y, int z, Climate.Sampler sampler);
    
    /**
     * 密度函数计算时调用
     * @param blockX 方块 X 坐标
     * @param blockY 方块 Y 坐标
     * @param blockZ 方块 Z 坐标
     * @param originalDensity 原始密度值
     * @return 如果返回非 null，使用脚本提供的密度值；否则使用原版逻辑
     */
    Double onDensityFunctionCompute(int blockX, int blockY, int blockZ, double originalDensity);
    
    /**
     * 地表规则更新时调用
     * @param blockX 方块 X 坐标
     * @param blockY 方块 Y 坐标
     * @param blockZ 方块 Z 坐标
     * @param stoneDepthAbove 上方石头深度
     * @param stoneDepthBelow 下方石头深度
     * @param waterHeight 水位高度
     */
    void onSurfaceRuleUpdate(int blockX, int blockY, int blockZ, int stoneDepthAbove, int stoneDepthBelow, int waterHeight);
    
    /**
     * 地表方块选择时调用
     * @param blockX 方块 X 坐标
     * @param blockY 方块 Y 坐标
     * @param blockZ 方块 Z 坐标
     * @param surfaceDepth 地表深度
     * @return 如果返回非 null，使用脚本提供的方块状态；否则使用原版逻辑
     */
    net.minecraft.world.level.block.state.BlockState onSurfaceBlockSelect(int blockX, int blockY, int blockZ, int surfaceDepth);
    
    // ===== 自定义维度相关方法 =====
    
    /**
     * 获取待加载的自定义维度配置列表
     * 在服务器启动时，世界加载前调用
     * @return 维度配置列表（已解析的配置对象）
     */
    java.util.List<DimensionConfigData> getCustomDimensionConfigs();
    
    /**
     * 维度配置数据传输对象
     * 用于在 Mixin 和插件之间传递维度配置
     */
    class DimensionConfigData {
        public String dimensionId;
        public DimensionTypeData typeConfig;
        public String generatorType;
        
        public static class DimensionTypeData {
            public Long fixedTime; // null 表示正常时间
            public boolean hasSkyLight;
            public boolean hasCeiling;
            public boolean ultraWarm;
            public boolean natural;
            public double coordinateScale;
            public boolean bedWorks;
            public boolean respawnAnchorWorks;
            public int minY;
            public int height;
            public int logicalHeight;
            public float ambientLight;
        }
    }
}
