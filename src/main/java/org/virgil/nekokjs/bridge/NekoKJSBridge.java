package org.virgil.nekokjs.bridge;

import org.virgil.nekokjs.mixin.bridge.Bridge;
import org.virgil.nekokjs.NekoKJSPlugin;
import org.virgil.nekokjs.event.EventManager;

/**
 * Bridge 实现类，用于 Mixin 和插件主类之间的通信
 * 这是 Leaves Mixin 插件开发的标准模式
 * 注意：此类在 main 模块中，可以直接访问插件类，无需反射
 */
public class NekoKJSBridge implements Bridge {
    private final NekoKJSPlugin plugin;

    public NekoKJSBridge(Object plugin) {
        this.plugin = (NekoKJSPlugin) plugin;
    }

    @Override
    public void onServerStarted() {
        plugin.getLogger().info("服务器已启动，NekoKJS 开始初始化脚本环境...");
        if (plugin.getScriptManager() != null) {
            plugin.getScriptManager().onServerStarted();
        }
    }

    @Override
    public void onServerTick() {
        EventManager eventManager = plugin.getEventManager();
        if (eventManager != null) {
            eventManager.onServerTick();
        }
    }

    @Override
    public Object getPlugin() {
        return plugin;
    }

    @Override
    public boolean onChunkSurfaceGenerate(net.minecraft.server.level.WorldGenRegion level, net.minecraft.world.level.chunk.ChunkAccess chunk) {
        EventManager eventManager = plugin.getEventManager();
        if (eventManager != null) {
            return eventManager.onChunkSurfaceGenerate(level, chunk);
        }
        return false;
    }

    @Override
    public java.util.concurrent.CompletableFuture<net.minecraft.world.level.chunk.ChunkAccess> onChunkNoiseGenerate(
        net.minecraft.world.level.chunk.ChunkAccess chunk, 
        net.minecraft.world.level.levelgen.RandomState randomState
    ) {
        // 暂不实现，返回 null 使用原版逻辑
        return null;
    }

    @Override
    public boolean onChunkCarverGenerate(
        net.minecraft.server.level.WorldGenRegion level, 
        net.minecraft.world.level.chunk.ChunkAccess chunk, 
        long seed
    ) {
        EventManager eventManager = plugin.getEventManager();
        if (eventManager != null) {
            return eventManager.onChunkCarverGenerate(level, chunk, seed);
        }
        return false;
    }

    @Override
    public java.util.List<Bridge.DimensionConfigData> getCustomDimensionConfigs() {
        // TODO: 实现维度配置获取
        // 暂时返回空列表，避免复杂的反射调用
        return java.util.Collections.emptyList();
    }

    @Override
    public net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome> onBiomeSelect(
        int x, int y, int z, 
        net.minecraft.world.level.biome.Climate.Sampler sampler
    ) {
        EventManager eventManager = plugin.getEventManager();
        if (eventManager != null) {
            return eventManager.onBiomeSelect(x, y, z, sampler);
        }
        return null;
    }

    @Override
    public Double onDensityFunctionCompute(int blockX, int blockY, int blockZ, double originalDensity) {
        EventManager eventManager = plugin.getEventManager();
        if (eventManager != null) {
            return eventManager.onDensityFunctionCompute(blockX, blockY, blockZ, originalDensity);
        }
        return null;
    }

    @Override
    public void onSurfaceRuleUpdate(int blockX, int blockY, int blockZ, int stoneDepthAbove, int stoneDepthBelow, int waterHeight) {
        EventManager eventManager = plugin.getEventManager();
        if (eventManager != null) {
            eventManager.onSurfaceRuleUpdate(blockX, blockY, blockZ, stoneDepthAbove, stoneDepthBelow, waterHeight);
        }
    }

    @Override
    public net.minecraft.world.level.block.state.BlockState onSurfaceBlockSelect(int blockX, int blockY, int blockZ, int surfaceDepth) {
        EventManager eventManager = plugin.getEventManager();
        if (eventManager != null) {
            return eventManager.onSurfaceBlockSelect(blockX, blockY, blockZ, surfaceDepth);
        }
        return null;
    }
}
