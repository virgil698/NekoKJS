package org.virgil.nekokjs.api.event;

import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.RandomState;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.ContextFactory;
import dev.latvian.mods.rhino.Function;
import dev.latvian.mods.rhino.Scriptable;
import org.virgil.nekokjs.NekoKJSPlugin;
import org.virgil.nekokjs.lang.LanguageManager;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Events API
 * 使用 NMS 反射动态注册事件监听器
 * 参考 KubeJS 的事件系统设计
 */
public class EventsAPI {
    private final NekoKJSPlugin plugin;
    private final Map<String, List<Function>> eventCallbacks;
    private final Listener dummyListener;
    private final ContextFactory contextFactory;
    private final LanguageManager lang;

    public EventsAPI(NekoKJSPlugin plugin) {
        this.plugin = plugin;
        this.eventCallbacks = new HashMap<>();
        this.dummyListener = new Listener() {};
        this.contextFactory = new ContextFactory();
        this.lang = plugin.getConfigManager().getLanguageManager();
    }

    /**
     * 监听玩家加入事件
     * 用法: Events.playerJoin(event => { ... })
     */
    public void playerJoin(Function callback) {
        registerEvent("org.bukkit.event.player.PlayerJoinEvent", callback);
    }

    /**
     * 监听玩家退出事件
     * 用法: Events.playerQuit(event => { ... })
     */
    public void playerQuit(Function callback) {
        registerEvent("org.bukkit.event.player.PlayerQuitEvent", callback);
    }

    /**
     * 监听玩家聊天事件
     * 用法: Events.playerChat(event => { ... })
     */
    public void playerChat(Function callback) {
        registerEvent("org.bukkit.event.player.AsyncPlayerChatEvent", callback);
    }

    /**
     * 监听服务器 Tick 事件
     * 用法: Events.serverTick(event => { ... })
     */
    public void serverTick(Function callback) {
        registerCallback("server.tick", callback);
    }

    /**
     * 监听方块破坏事件
     * 用法: Events.blockBreak(event => { ... })
     */
    public void blockBreak(Function callback) {
        registerEvent("org.bukkit.event.block.BlockBreakEvent", callback);
    }

    /**
     * 监听方块放置事件
     * 用法: Events.blockPlace(event => { ... })
     */
    public void blockPlace(Function callback) {
        registerEvent("org.bukkit.event.block.BlockPlaceEvent", callback);
    }

    /**
     * 监听实体死亡事件
     * 用法: Events.entityDeath(event => { ... })
     */
    public void entityDeath(Function callback) {
        registerEvent("org.bukkit.event.entity.EntityDeathEvent", callback);
    }

    /**
     * 监听玩家交互事件
     * 用法: Events.playerInteract(event => { ... })
     */
    public void playerInteract(Function callback) {
        registerEvent("org.bukkit.event.player.PlayerInteractEvent", callback);
    }

    /**
     * 通用事件注册方法
     * 使用反射动态注册任意 Bukkit 事件
     * 
     * @param eventClassName 事件类的完整名称
     * @param callback JavaScript 回调函数
     */
    public void on(String eventClassName, Function callback) {
        registerEvent(eventClassName, callback);
    }

    /**
     * 使用反射注册事件监听器
     */
    private void registerEvent(String eventClassName, Function callback) {
        try {
            // 加载事件类
            Class<?> eventClass = Class.forName(eventClassName);
            
            // 检查是否是 Event 的子类
            if (!Event.class.isAssignableFrom(eventClass)) {
                plugin.getLogger().warning(eventClassName + " is not an Event class");
                return;
            }

            @SuppressWarnings("unchecked")
            Class<? extends Event> event = (Class<? extends Event>) eventClass;
            
            // 注册回调
            registerCallback(eventClassName, callback);
            
            // 创建事件执行器
            EventExecutor executor = (listener, eventInstance) -> {
                if (event.isInstance(eventInstance)) {
                    executeCallbacks(eventClassName, eventInstance);
                }
            };
            
            // 注册到 Bukkit 事件系统
            Bukkit.getPluginManager().registerEvent(
                event,
                dummyListener,
                EventPriority.NORMAL,
                executor,
                plugin
            );
            
            plugin.getLogger().info(lang.eventRegistered(eventClass.getSimpleName()));
        } catch (ClassNotFoundException e) {
            plugin.getLogger().warning(lang.eventNotFound(eventClassName));
        } catch (Exception e) {
            plugin.getLogger().severe(lang.eventRegisterFailed(e.getMessage()));
            e.printStackTrace();
        }
    }

    /**
     * 注册回调函数
     */
    private void registerCallback(String eventName, Function callback) {
        eventCallbacks.computeIfAbsent(eventName, k -> new ArrayList<>()).add(callback);
    }

    /**
     * 执行回调函数
     */
    private void executeCallbacks(String eventName, Object event) {
        List<Function> callbacks = eventCallbacks.get(eventName);
        if (callbacks == null || callbacks.isEmpty()) {
            return;
        }

        Context ctx = contextFactory.enter();
        try {
            Scriptable scope = ctx.initStandardObjects();
            
            // 注入 PlayerMessageHelper 到作用域，提供安全的 sendMessage 方法
            Object helper = ctx.javaToJS(PlayerMessageHelper.class, scope);
            scope.put(ctx, "PlayerMessageHelper", scope, helper);
            
            for (Function callback : callbacks) {
                try {
                    // 将 Java 对象包装为 JS 对象
                    Object jsEvent = ctx.javaToJS(event, scope);
                    
                    // 如果是 PlayerEvent，额外提供一个包装的 player 对象
                    if (event instanceof org.bukkit.event.player.PlayerEvent playerEvent) {
                        Scriptable eventObj = (Scriptable) jsEvent;
                        // 创建一个包装函数来安全地调用 sendMessage
                        Object wrappedPlayer = ctx.javaToJS(playerEvent.getPlayer(), scope);
                        eventObj.put(ctx, "_player", eventObj, wrappedPlayer);
                    }
                    
                    callback.call(ctx, scope, scope, new Object[]{jsEvent});
                } catch (Exception e) {
                    plugin.getLogger().severe(lang.eventCallbackFailed(eventName, e.getMessage()));
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe(lang.eventCallbacksFailed(eventName, e.getMessage()));
            e.printStackTrace();
        }
    }

    /**
     * 触发服务器 Tick 事件
     * 由 EventManager 调用
     */
    public void triggerServerTick() {
        executeCallbacks("server.tick", null);
    }
    
    // ===== 世界生成相关方法 =====
    
    /**
     * 监听区块地表生成事件
     * 用法: Events.chunkSurfaceGenerate((level, chunk) => { ... })
     */
    public void chunkSurfaceGenerate(Function callback) {
        registerCallback("worldgen.chunk.surface", callback);
    }
    
    /**
     * 触发区块地表生成事件
     * 由 EventManager 调用
     * @return true 表示脚本已处理，取消原版逻辑
     */
    public boolean triggerChunkSurfaceGenerate(WorldGenRegion level, ChunkAccess chunk) {
        List<Function> callbacks = eventCallbacks.get("worldgen.chunk.surface");
        if (callbacks == null || callbacks.isEmpty()) {
            return false;
        }
        
        Context ctx = contextFactory.enter();
        try {
            Scriptable scope = ctx.initStandardObjects();
            boolean handled = false;
            
            for (Function callback : callbacks) {
                Object result = callback.call(ctx, scope, scope, new Object[]{level, chunk});
                // 如果任何回调返回 true，表示已处理
                if (result instanceof Boolean && (Boolean) result) {
                    handled = true;
                }
            }
            
            return handled;
        } catch (Exception e) {
            plugin.getLogger().severe("Error in chunk surface generation callback: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            // Context 会自动清理
        }
    }
    
    /**
     * 监听区块噪声填充事件
     * 用法: Events.chunkNoiseGenerate((chunk, randomState) => { ... })
     */
    public void chunkNoiseGenerate(Function callback) {
        registerCallback("worldgen.chunk.noise", callback);
    }
    
    /**
     * 触发区块噪声填充事件
     * 由 EventManager 调用
     * @return 如果脚本提供了自定义结果，返回 CompletableFuture；否则返回 null
     */
    @Nullable
    public CompletableFuture<ChunkAccess> triggerChunkNoiseGenerate(ChunkAccess chunk, RandomState randomState) {
        List<Function> callbacks = eventCallbacks.get("worldgen.chunk.noise");
        if (callbacks == null || callbacks.isEmpty()) {
            return null;
        }
        
        // 注意：噪声生成是高频操作，暂时返回 null 使用原版逻辑
        // 如果需要自定义，可以在这里实现
        return null;
    }
    
    /**
     * 监听洞穴雕刻事件
     * 用法: Events.chunkCarverGenerate((level, chunk, seed) => { ... })
     */
    public void chunkCarverGenerate(Function callback) {
        registerCallback("worldgen.chunk.carver", callback);
    }
    
    /**
     * 触发洞穴雕刻事件
     * 由 EventManager 调用
     * @return true 表示脚本已处理，取消原版逻辑
     */
    public boolean triggerChunkCarverGenerate(WorldGenRegion level, ChunkAccess chunk, long seed) {
        List<Function> callbacks = eventCallbacks.get("worldgen.chunk.carver");
        if (callbacks == null || callbacks.isEmpty()) {
            return false;
        }
        
        Context ctx = contextFactory.enter();
        try {
            Scriptable scope = ctx.initStandardObjects();
            boolean handled = false;
            
            for (Function callback : callbacks) {
                Object result = callback.call(ctx, scope, scope, new Object[]{level, chunk, seed});
                if (result instanceof Boolean && (Boolean) result) {
                    handled = true;
                }
            }
            
            return handled;
        } catch (Exception e) {
            plugin.getLogger().severe("Error in chunk carver generation callback: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            // Context 会自动清理
        }
    }
    
    /**
     * 监听生物群系选择事件
     * 用法: Events.biomeSelect((x, y, z, sampler) => { return "minecraft:plains"; })
     */
    public void biomeSelect(Function callback) {
        registerCallback("worldgen.biome.select", callback);
    }
    
    /**
     * 触发生物群系选择事件
     * 由 EventManager 调用
     * @return 如果脚本提供了自定义生物群系，返回 Holder<Biome>；否则返回 null
     */
    @Nullable
    public Holder<Biome> triggerBiomeSelect(int x, int y, int z, Climate.Sampler sampler) {
        List<Function> callbacks = eventCallbacks.get("worldgen.biome.select");
        if (callbacks == null || callbacks.isEmpty()) {
            return null;
        }
        
        // 注意：生物群系选择是极高频操作，暂时返回 null 使用原版逻辑
        // 如果需要自定义，需要非常小心性能问题
        return null;
    }

    // ===== 高级世界生成钩子 =====
    
    /**
     * 监听世界加载前事件
     * 用法: Events.worldLoad((world) => { ... })
     */
    public void worldLoad(Function callback) {
        registerEvent("org.bukkit.event.world.WorldLoadEvent", callback);
    }
    
    /**
     * 监听世界初始化事件
     * 用法: Events.worldInit((world) => { ... })
     */
    public void worldInit(Function callback) {
        registerEvent("org.bukkit.event.world.WorldInitEvent", callback);
    }
    
    /**
     * 监听区块加载事件
     * 用法: Events.chunkLoad((chunk) => { ... })
     */
    public void chunkLoad(Function callback) {
        registerEvent("org.bukkit.event.world.ChunkLoadEvent", callback);
    }
    
    /**
     * 监听区块卸载事件
     * 用法: Events.chunkUnload((chunk) => { ... })
     */
    public void chunkUnload(Function callback) {
        registerEvent("org.bukkit.event.world.ChunkUnloadEvent", callback);
    }
    
    /**
     * 监听区块生成完成事件（包含所有装饰）
     * 用法: Events.chunkPopulate((chunk) => { ... })
     */
    public void chunkPopulate(Function callback) {
        registerEvent("org.bukkit.event.world.ChunkPopulateEvent", callback);
    }
    
    /**
     * 监听结构生成事件
     * 用法: Events.structureGenerate((world, chunk, structure) => { ... })
     */
    public void structureGenerate(Function callback) {
        registerEvent("org.bukkit.event.world.StructureGrowEvent", callback);
    }
    
    /**
     * 监听方块形成事件（如冰、雪形成）
     * 用法: Events.blockForm((block, newState) => { ... })
     */
    public void blockForm(Function callback) {
        registerEvent("org.bukkit.event.block.BlockFormEvent", callback);
    }
    
    /**
     * 监听方块蔓延事件（如火、藤蔓蔓延）
     * 用法: Events.blockSpread((block, source) => { ... })
     */
    public void blockSpread(Function callback) {
        registerEvent("org.bukkit.event.block.BlockSpreadEvent", callback);
    }
    
    /**
     * 监听方块生长事件（如作物、树苗生长）
     * 用法: Events.blockGrow((block) => { ... })
     */
    public void blockGrow(Function callback) {
        registerEvent("org.bukkit.event.block.BlockGrowEvent", callback);
    }
    
    /**
     * 监听生物生成事件
     * 用法: Events.creatureSpawn((entity, location, reason) => { ... })
     */
    public void creatureSpawn(Function callback) {
        registerEvent("org.bukkit.event.entity.CreatureSpawnEvent", callback);
    }
    
    /**
     * 监听天气变化事件
     * 用法: Events.weatherChange((world, toWeather) => { ... })
     */
    public void weatherChange(Function callback) {
        registerEvent("org.bukkit.event.weather.WeatherChangeEvent", callback);
    }
    
    /**
     * 监听雷击事件
     * 用法: Events.lightningStrike((lightning) => { ... })
     */
    public void lightningStrike(Function callback) {
        registerEvent("org.bukkit.event.weather.LightningStrikeEvent", callback);
    }
    
    /**
     * 清理所有事件监听器
     */
    public void cleanup() {
        // 注销所有事件
        try {
            HandlerList.unregisterAll(dummyListener);
        } catch (Exception e) {
            plugin.getLogger().warning(lang.eventCleanupFailed(e.getMessage()));
        }
        
        eventCallbacks.clear();
        plugin.getLogger().info(lang.eventListenersCleaned());
    }

    /**
     * 获取已注册的事件数量
     */
    public int getRegisteredEventCount() {
        return eventCallbacks.size();
    }

    /**
     * 获取指定事件的回调数量
     */
    public int getCallbackCount(String eventName) {
        List<Function> callbacks = eventCallbacks.get(eventName);
        return callbacks != null ? callbacks.size() : 0;
    }
    
    /**
     * 触发密度函数计算事件
     * 
     * @param blockX X 坐标
     * @param blockY Y 坐标
     * @param blockZ Z 坐标
     * @param originalDensity 原始密度值
     * @return 自定义密度值，null 表示使用原版逻辑
     */
    @Nullable
    public Double triggerDensityFunctionCompute(int blockX, int blockY, int blockZ, double originalDensity) {
        List<Function> callbacks = eventCallbacks.get("worldgen.density");
        if (callbacks == null || callbacks.isEmpty()) {
            return null;
        }
        
        Context cx = contextFactory.enter();
        try {
            Scriptable scope = cx.initStandardObjects();
            
            // 创建事件对象
            Scriptable event = cx.newObject(scope);
            event.put(cx, "x", event, blockX);
            event.put(cx, "y", event, blockY);
            event.put(cx, "z", event, blockZ);
            event.put(cx, "density", event, originalDensity);
            event.put(cx, "cancelled", event, false);
            
            // 执行回调
            for (Function callback : callbacks) {
                Object result = callback.call(cx, scope, scope, new Object[]{event});
                
                // 如果返回了数字，使用自定义密度
                if (result instanceof Number) {
                    return ((Number) result).doubleValue();
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Density function event error: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * 触发地表规则更新事件
     * 
     * @param blockX X 坐标
     * @param blockY Y 坐标
     * @param blockZ Z 坐标
     * @param stoneDepthAbove 上方石头深度
     * @param stoneDepthBelow 下方石头深度
     * @param waterHeight 水位高度
     */
    public void triggerSurfaceRuleUpdate(int blockX, int blockY, int blockZ, 
                                        int stoneDepthAbove, int stoneDepthBelow, int waterHeight) {
        List<Function> callbacks = eventCallbacks.get("worldgen.surface.update");
        if (callbacks == null || callbacks.isEmpty()) {
            return;
        }
        
        Context cx = contextFactory.enter();
        try {
            Scriptable scope = cx.initStandardObjects();
            
            // 创建事件对象
            Scriptable event = cx.newObject(scope);
            event.put(cx, "x", event, blockX);
            event.put(cx, "y", event, blockY);
            event.put(cx, "z", event, blockZ);
            event.put(cx, "stoneDepthAbove", event, stoneDepthAbove);
            event.put(cx, "stoneDepthBelow", event, stoneDepthBelow);
            event.put(cx, "waterHeight", event, waterHeight);
            
            // 执行回调
            for (Function callback : callbacks) {
                callback.call(cx, scope, scope, new Object[]{event});
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Surface rule update event error: " + e.getMessage());
        }
    }
    
    /**
     * 触发地表方块选择事件
     * 
     * @param blockX X 坐标
     * @param blockY Y 坐标
     * @param blockZ Z 坐标
     * @param surfaceDepth 地表深度
     * @return 自定义方块状态，null 表示使用原版逻辑
     */
    @Nullable
    public net.minecraft.world.level.block.state.BlockState triggerSurfaceBlockSelect(
            int blockX, int blockY, int blockZ, int surfaceDepth) {
        List<Function> callbacks = eventCallbacks.get("worldgen.surface.block");
        if (callbacks == null || callbacks.isEmpty()) {
            return null;
        }
        
        Context cx = contextFactory.enter();
        try {
            Scriptable scope = cx.initStandardObjects();
            
            // 创建事件对象
            Scriptable event = cx.newObject(scope);
            event.put(cx, "x", event, blockX);
            event.put(cx, "y", event, blockY);
            event.put(cx, "z", event, blockZ);
            event.put(cx, "surfaceDepth", event, surfaceDepth);
            event.put(cx, "block", event, null);
            
            // 执行回调
            for (Function callback : callbacks) {
                callback.call(cx, scope, scope, new Object[]{event});
                
                // 检查是否设置了自定义方块
                Object blockObj = event.get(cx, "block", event);
                if (blockObj instanceof net.minecraft.world.level.block.state.BlockState) {
                    return (net.minecraft.world.level.block.state.BlockState) blockObj;
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Surface block select event error: " + e.getMessage());
        }
        
        return null;
    }
}
