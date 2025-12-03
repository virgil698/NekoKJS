package org.virgil.nekokjs.event;

import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.RandomState;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.virgil.nekokjs.NekoKJSPlugin;
import org.virgil.nekokjs.api.event.EventsAPI;
import org.virgil.nekokjs.lang.LanguageManager;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * 事件管理器
 * 使用 EventsAPI 进行事件处理
 */
public class EventManager implements Listener {
    private final NekoKJSPlugin plugin;
    private final Logger logger;
    private final EventsAPI eventsAPI;
    private final LanguageManager lang;
    private int tickInterval;
    private long tickCount = 0;

    public EventManager(NekoKJSPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.lang = plugin.getConfigManager().getLanguageManager();
        this.eventsAPI = new EventsAPI(plugin);
        
        // 注册事件监听器
        Bukkit.getPluginManager().registerEvents(this, plugin);
        
        // 从配置读取 tick 间隔
        if (plugin.getConfigManager() != null) {
            tickInterval = plugin.getConfigManager().getConfig().getInt("events.tick-interval", 20);
        }
        
        logger.info(lang.eventManagerInitialized(tickInterval));
    }

    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        logger.info(lang.eventServerLoaded(event.getType().toString()));
    }

    /**
     * 服务器 Tick 事件
     * 由 Mixin 注入调用
     */
    public void onServerTick() {
        tickCount++;
        
        // 根据配置的间隔触发 tick 事件
        if (tickCount % tickInterval == 0) {
            try {
                eventsAPI.triggerServerTick();
            } catch (Exception e) {
                logger.warning(lang.eventTickFailed(e.getMessage()));
            }
        }
    }

    public long getTickCount() {
        return tickCount;
    }

    public EventsAPI getEventsAPI() {
        return eventsAPI;
    }

    public void cleanup() {
        eventsAPI.cleanup();
        logger.info(lang.eventManagerCleaned());
    }
    
    // ===== 世界生成相关方法 =====
    
    /**
     * 区块地表生成事件
     * 由 Mixin 调用
     */
    public boolean onChunkSurfaceGenerate(WorldGenRegion level, ChunkAccess chunk) {
        try {
            return eventsAPI.triggerChunkSurfaceGenerate(level, chunk);
        } catch (Exception e) {
            logger.warning("Chunk surface generation failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 区块噪声填充事件
     * 由 Mixin 调用
     */
    @Nullable
    public CompletableFuture<ChunkAccess> onChunkNoiseGenerate(ChunkAccess chunk, RandomState randomState) {
        try {
            return eventsAPI.triggerChunkNoiseGenerate(chunk, randomState);
        } catch (Exception e) {
            logger.warning("Chunk noise generation failed: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 洞穴雕刻事件
     * 由 Mixin 调用
     */
    public boolean onChunkCarverGenerate(WorldGenRegion level, ChunkAccess chunk, long seed) {
        try {
            return eventsAPI.triggerChunkCarverGenerate(level, chunk, seed);
        } catch (Exception e) {
            logger.warning("Chunk carver generation failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 生物群系选择事件
     * 由 Mixin 调用
     */
    @Nullable
    public Holder<Biome> onBiomeSelect(int x, int y, int z, Climate.Sampler sampler) {
        try {
            return eventsAPI.triggerBiomeSelect(x, y, z, sampler);
        } catch (Exception e) {
            logger.warning("Biome selection failed: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 密度函数计算事件
     * 由 Mixin 调用
     */
    @Nullable
    public Double onDensityFunctionCompute(int blockX, int blockY, int blockZ, double originalDensity) {
        try {
            return eventsAPI.triggerDensityFunctionCompute(blockX, blockY, blockZ, originalDensity);
        } catch (Exception e) {
            // 忽略错误，密度函数是高频调用
            return null;
        }
    }
    
    /**
     * 地表规则更新事件
     * 由 Mixin 调用
     */
    public void onSurfaceRuleUpdate(int blockX, int blockY, int blockZ, int stoneDepthAbove, int stoneDepthBelow, int waterHeight) {
        try {
            eventsAPI.triggerSurfaceRuleUpdate(blockX, blockY, blockZ, stoneDepthAbove, stoneDepthBelow, waterHeight);
        } catch (Exception e) {
            // 忽略错误
        }
    }
    
    /**
     * 地表方块选择事件
     * 由 Mixin 调用
     */
    @Nullable
    public net.minecraft.world.level.block.state.BlockState onSurfaceBlockSelect(int blockX, int blockY, int blockZ, int surfaceDepth) {
        try {
            return eventsAPI.triggerSurfaceBlockSelect(blockX, blockY, blockZ, surfaceDepth);
        } catch (Exception e) {
            // 忽略错误
            return null;
        }
    }
}
