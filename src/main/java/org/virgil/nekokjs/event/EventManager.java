package org.virgil.nekokjs.event;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.virgil.nekokjs.NekoKJSPlugin;
import org.virgil.nekokjs.api.EventsAPI;
import org.virgil.nekokjs.lang.LanguageManager;

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
}
