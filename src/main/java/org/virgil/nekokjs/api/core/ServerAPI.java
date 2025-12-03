package org.virgil.nekokjs.api.core;

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
     * 广播消息给所有玩家（别名方法）
     * 与 Bukkit API 命名保持一致
     */
    public void broadcastMessage(String message) {
        server.broadcastMessage(message);
    }

    /**
     * 执行控制台命令
     * Folia 兼容：使用全局区域调度器
     */
    public boolean runCommand(String command) {
        // Folia 兼容：检查是否有全局区域调度器
        try {
            // 尝试使用 Folia 的全局区域调度器
            var globalRegionScheduler = Bukkit.getGlobalRegionScheduler();
            globalRegionScheduler.execute(plugin, () -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            });
            return true;
        } catch (NoSuchMethodError e) {
            // Paper/Spigot：直接执行命令
            return Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    }

    /**
     * 以玩家身份执行命令
     * @param player 执行命令的玩家
     * @param command 命令（不带 /）
     * @return 是否执行成功
     */
    public boolean runCommandAsPlayer(Player player, String command) {
        return server.dispatchCommand(player, command);
    }

    /**
     * 执行多条命令
     * @param commands 命令数组
     * @return 成功执行的命令数量
     */
    public int runCommands(String... commands) {
        int success = 0;
        for (String command : commands) {
            if (runCommand(command)) {
                success++;
            }
        }
        return success;
    }

    /**
     * 延迟执行命令（tick）
     * @param command 命令
     * @param delay 延迟时间（tick）
     */
    public void runCommandLater(String command, long delay) {
        try {
            // Folia：使用全局区域调度器
            var globalRegionScheduler = Bukkit.getGlobalRegionScheduler();
            globalRegionScheduler.runDelayed(plugin, task -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            }, delay);
        } catch (NoSuchMethodError e) {
            // Paper/Spigot：使用传统调度器
            server.getScheduler().runTaskLater(plugin, () -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            }, delay);
        }
    }

    /**
     * 延迟执行任务（tick）
     * JavaScript 可以传入回调函数
     * @param task 要执行的任务（Runnable 或 JavaScript 函数）
     * @param delay 延迟时间（tick）
     */
    public void runTaskLater(Object task, long delay) {
        try {
            // Folia：使用全局区域调度器
            var globalRegionScheduler = Bukkit.getGlobalRegionScheduler();
            globalRegionScheduler.runDelayed(plugin, scheduledTask -> {
                executeTask(task);
            }, delay);
        } catch (NoSuchMethodError e) {
            // Paper/Spigot：使用传统调度器
            server.getScheduler().runTaskLater(plugin, () -> {
                executeTask(task);
            }, delay);
        }
    }

    /**
     * 执行任务（支持 Runnable 和 JavaScript 函数）
     */
    private void executeTask(Object task) {
        if (task instanceof Runnable runnable) {
            runnable.run();
        } else if (task instanceof dev.latvian.mods.rhino.Function function) {
            try {
                var contextFactory = new dev.latvian.mods.rhino.ContextFactory();
                var ctx = contextFactory.enter();
                var scope = ctx.initStandardObjects();
                function.call(ctx, scope, scope, new Object[0]);
            } catch (Exception e) {
                plugin.getLogger().warning("执行延迟任务时出错: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * 定时重复执行命令
     * @param command 命令
     * @param delay 初始延迟（tick）
     * @param period 重复间隔（tick）
     */
    public void runCommandTimer(String command, long delay, long period) {
        try {
            // Folia：使用全局区域调度器
            var globalRegionScheduler = Bukkit.getGlobalRegionScheduler();
            globalRegionScheduler.runAtFixedRate(plugin, task -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            }, delay, period);
        } catch (NoSuchMethodError e) {
            // Paper/Spigot：使用传统调度器
            server.getScheduler().runTaskTimer(plugin, () -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            }, delay, period);
        }
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

    /**
     * 获取当前世界时间（主世界）
     * @return 游戏内时间（0-24000）
     */
    public long getWorldTime() {
        return server.getWorlds().get(0).getTime();
    }

    /**
     * 获取指定世界的时间
     * @param worldName 世界名称
     * @return 游戏内时间（0-24000），如果世界不存在返回 -1
     */
    public long getWorldTime(String worldName) {
        var world = server.getWorld(worldName);
        return world != null ? world.getTime() : -1;
    }

    /**
     * 设置世界时间（主世界）
     * @param time 游戏内时间（0-24000）
     */
    public void setWorldTime(long time) {
        server.getWorlds().get(0).setTime(time);
    }

    /**
     * 设置指定世界的时间
     * @param worldName 世界名称
     * @param time 游戏内时间（0-24000）
     */
    public void setWorldTime(String worldName, long time) {
        var world = server.getWorld(worldName);
        if (world != null) {
            world.setTime(time);
        }
    }

    /**
     * 获取系统当前时间戳（毫秒）
     * @return Unix 时间戳
     */
    public long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * 获取服务器运行时间（毫秒）
     * 从服务器启动到现在的时间
     */
    public long getUptime() {
        return System.currentTimeMillis() - server.getWorlds().get(0).getGameTime() * 50L;
    }

    /**
     * 获取游戏刻数（主世界）
     * @return 游戏刻数
     */
    public long getGameTime() {
        return server.getWorlds().get(0).getGameTime();
    }

    /**
     * 获取完整日期时间（主世界）
     * @return 游戏内完整时间（天数）
     */
    public long getFullTime() {
        return server.getWorlds().get(0).getFullTime();
    }
}
