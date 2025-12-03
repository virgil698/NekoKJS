// 玩家退出处理器
// Player Quit Handler

/**
 * 处理玩家退出事件
 * @param {PlayerQuitEvent} event - 玩家退出事件
 * @param {Object} config - 配置对象
 * @param {Object} formatter - 格式化工具
 */
function handlePlayerQuit(event, config, formatter) {
    if (!config.enabled || !config.quitMessage.enabled) {
        return;
    }
    
    const player = event.getPlayer();
    
    // 获取显示名称（转换为纯文本）
    let displayName = player.getName();
    try {
        const displayNameComponent = player.displayName();
        if (displayNameComponent) {
            const PlainTextComponentSerializer = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
            displayName = PlainTextComponentSerializer.plainText().serialize(displayNameComponent);
        }
    } catch (e) {
        // 使用默认名称
    }
    
    const placeholders = {
        "player": player.getName(),
        "displayname": displayName,
        "online": Server.getOnlinePlayerCount() - 1, // 减去当前退出的玩家
        "world": player.getWorld().getName()
    };
    
    // 全服广播退出消息
    if (config.quitMessage.broadcast) {
        const broadcast = formatter.format(config.quitMessage.broadcast, placeholders);
        Message.broadcast(broadcast);
    }
}

// 导出
if (typeof module !== 'undefined' && module.exports) {
    module.exports = { handlePlayerQuit };
}
