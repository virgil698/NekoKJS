package org.virgil.nekokjs.api;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.virgil.nekokjs.NekoKJSPlugin;

import java.util.Collection;

/**
 * Server API
 * 提供服务器相关操作的简化接口
 */
public class ServerAPI {
    private final NekoKJSPlugin plugin;
    private final Server server;

    public ServerAPI(NekoKJSPlugin plugin) {
        this.plugin = plugin;
        this.server = plugin.getServer();
    }

    /**
     * 获取服务器名称
     */
    public String getName() {
        return server.getName();
    }

    /**
     * 获取服务器版本
     */
    public String getVersion() {
        return server.getVersion();
    }

    /**
     * 获取在线玩家数量
     */
    public int getOnlinePlayerCount() {
        return server.getOnlinePlayers().size();
    }

    /**
     * 获取所有在线玩家
     */
    public Collection<? extends Player> getOnlinePlayers() {
        return server.getOnlinePlayers();
    }

    /**
     * 广播消息给所有玩家
     */
    public void broadcast(String message) {
        server.broadcastMessage(message);
    }

    /**
     * 执行控制台命令
     */
    public boolean runCommand(String command) {
        return Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    /**
     * 获取最大玩家数
     */
    public int getMaxPlayers() {
        return server.getMaxPlayers();
    }

    /**
     * 获取 MOTD
     */
    public String getMotd() {
        return server.getMotd();
    }

    /**
     * 设置 MOTD
     */
    public void setMotd(String motd) {
        server.setMotd(motd);
    }

    /**
     * 获取当前 TPS（需要通过反射或 Paper API）
     */
    public double[] getTPS() {
        try {
            return server.getTPS();
        } catch (Exception e) {
            return new double[]{20.0, 20.0, 20.0};
        }
    }
}
