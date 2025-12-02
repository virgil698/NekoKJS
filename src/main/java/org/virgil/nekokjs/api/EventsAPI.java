package org.virgil.nekokjs.api;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            for (Function callback : callbacks) {
                try {
                    // Function.call 需要: Context, Scriptable, Scriptable, Object[]
                    // 将 Java 对象包装为 JS 对象
                    Object jsEvent = ctx.javaToJS(event, scope);
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

    /**
     * 清理所有事件监听器
     */
    public void cleanup() {
        // 注销所有事件
        try {
            HandlerList.unregisterAll(dummyListener);
        } catch (Exception e) {
            plugin.getLogger().warning("清理事件监听器失败: " + e.getMessage());
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
}
