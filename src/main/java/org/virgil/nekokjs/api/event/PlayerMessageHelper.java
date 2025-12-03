package org.virgil.nekokjs.api.event;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

/**
 * Player 消息辅助类
 * 提供明确的方法签名以避免 Rhino 的方法歧义问题
 */
public class PlayerMessageHelper {
    
    /**
     * 安全地发送消息给玩家
     * 此方法提供明确的签名，避免与 Player.sendMessage 的重载方法产生歧义
     * 
     * @param player 玩家对象
     * @param message 消息文本
     */
    public static void sendMessage(Player player, String message) {
        player.sendMessage(Component.text(message));
    }
    
    /**
     * 发送多行消息
     * 
     * @param player 玩家对象
     * @param messages 消息数组
     */
    public static void sendMessages(Player player, String... messages) {
        for (String message : messages) {
            player.sendMessage(Component.text(message));
        }
    }
}
