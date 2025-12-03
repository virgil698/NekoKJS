package org.virgil.nekokjs.api.core;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.Server;

/**
 * Message API
 * 提供 MiniMessage 格式消息发送功能
 */
public class MessageAPI {
    private final Server server;
    private final MiniMessage miniMessage;

    public MessageAPI(Server server) {
        this.server = server;
        this.miniMessage = MiniMessage.miniMessage();
    }

    /**
     * 解析 MiniMessage 格式文本为 Component
     * 
     * @param message MiniMessage 格式的文本，例如: "<red>红色文本 <bold>粗体"
     * @return 解析后的 Component
     */
    public Component parse(String message) {
        return miniMessage.deserialize(message);
    }

    /**
     * 向玩家发送 MiniMessage 格式消息
     * 
     * @param player 目标玩家
     * @param message MiniMessage 格式的消息
     */
    public void send(Player player, String message) {
        Component component = miniMessage.deserialize(message);
        player.sendMessage(component);
    }

    /**
     * 向所有在线玩家广播 MiniMessage 格式消息
     * 
     * @param message MiniMessage 格式的消息
     */
    public void broadcast(String message) {
        Component component = miniMessage.deserialize(message);
        server.broadcast(component);
    }

    /**
     * 向玩家发送 ActionBar 消息
     * 
     * @param player 目标玩家
     * @param message MiniMessage 格式的消息
     */
    public void sendActionBar(Player player, String message) {
        Component component = miniMessage.deserialize(message);
        player.sendActionBar(component);
    }

    /**
     * 向玩家发送 Title
     * 
     * @param player 目标玩家
     * @param title 标题（MiniMessage 格式）
     * @param subtitle 副标题（MiniMessage 格式）
     * @param fadeIn 淡入时间（tick）
     * @param stay 停留时间（tick）
     * @param fadeOut 淡出时间（tick）
     */
    public void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        Component titleComponent = miniMessage.deserialize(title);
        Component subtitleComponent = miniMessage.deserialize(subtitle);
        player.showTitle(net.kyori.adventure.title.Title.title(
            titleComponent,
            subtitleComponent,
            net.kyori.adventure.title.Title.Times.times(
                java.time.Duration.ofMillis(fadeIn * 50L),
                java.time.Duration.ofMillis(stay * 50L),
                java.time.Duration.ofMillis(fadeOut * 50L)
            )
        ));
    }

    /**
     * 向玩家发送简单 Title（使用默认时间）
     * 
     * @param player 目标玩家
     * @param title 标题（MiniMessage 格式）
     * @param subtitle 副标题（MiniMessage 格式）
     */
    public void sendTitle(Player player, String title, String subtitle) {
        sendTitle(player, title, subtitle, 10, 70, 20);
    }
}
