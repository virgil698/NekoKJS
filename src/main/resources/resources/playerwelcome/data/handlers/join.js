// 玩家加入处理器
// Player Join Handler

/**
 * 处理玩家加入事件
 * @param {PlayerJoinEvent} event - 玩家加入事件
 * @param {Object} config - 配置对象
 * @param {Object} storage - 存储工具
 * @param {Object} formatter - 格式化工具
 */
function handlePlayerJoin(event, config, storage, formatter) {
    if (!config.enabled) {
        return;
    }
    
    const player = event.getPlayer();
    const isFirstJoin = storage.isFirstJoin(player);
    const placeholders = formatter.getPlayerPlaceholders(player, storage);
    
    // 处理首次加入
    if (isFirstJoin && config.firstJoin.enabled) {
        handleFirstJoin(player, config, placeholders);
        storage.markJoined(player);
        return;
    }
    
    // 处理普通加入
    handleNormalJoin(player, config, placeholders);
}

/**
 * 处理首次加入
 */
function handleFirstJoin(player, config, placeholders) {
    const firstJoinConfig = config.firstJoin;
    
    // 延迟发送消息，确保玩家完全加载
    Server.runTaskLater(() => {
        // 发送欢迎消息
        if (firstJoinConfig.messages && firstJoinConfig.messages.length > 0) {
            const messages = MessageFormatter.formatArray(firstJoinConfig.messages, placeholders);
            messages.forEach(msg => {
                Message.send(player, msg);
            });
        }
        
        // 显示标题
        if (firstJoinConfig.title) {
            const title = MessageFormatter.format(firstJoinConfig.title, placeholders);
            const subtitle = MessageFormatter.format(firstJoinConfig.subtitle, placeholders);
            Message.sendTitle(
                player,
                title,
                subtitle,
                firstJoinConfig.fadeIn,
                firstJoinConfig.stay,
                firstJoinConfig.fadeOut
            );
        }
        
        // 播放音效
        if (config.sounds.enabled && config.sounds.firstJoinSound) {
            playSound(player, config.sounds.firstJoinSound, config.sounds);
        }
    }, 20); // 延迟 1 秒
    
    // 全服广播
    if (firstJoinConfig.broadcast) {
        const broadcast = MessageFormatter.format(firstJoinConfig.broadcast, placeholders);
        Message.broadcast(broadcast);
    }
}

/**
 * 处理普通加入
 */
function handleNormalJoin(player, config, placeholders) {
    // 发送聊天消息
    if (config.chatMessages.enabled) {
        Server.runTaskLater(() => {
            const messages = MessageFormatter.formatArray(config.chatMessages.messages, placeholders);
            messages.forEach(msg => {
                Message.send(player, msg);
            });
        }, config.chatMessages.delay);
    }
    
    // 显示标题
    if (config.titleMessage.enabled) {
        Server.runTaskLater(() => {
            const title = MessageFormatter.format(config.titleMessage.title, placeholders);
            const subtitle = MessageFormatter.format(config.titleMessage.subtitle, placeholders);
            Message.sendTitle(
                player,
                title,
                subtitle,
                config.titleMessage.fadeIn,
                config.titleMessage.stay,
                config.titleMessage.fadeOut
            );
        }, config.titleMessage.delay);
    }
    
    // 播放音效
    if (config.sounds.enabled && config.sounds.joinSound) {
        Server.runTaskLater(() => {
            playSound(player, config.sounds.joinSound, config.sounds);
        }, 10);
    }
}

/**
 * 播放音效
 */
function playSound(player, soundName, soundConfig) {
    try {
        const Sound = org.bukkit.Sound;
        const sound = Sound.valueOf(soundName);
        player.playSound(
            player.getLocation(),
            sound,
            soundConfig.volume,
            soundConfig.pitch
        );
    } catch (e) {
        console.warn("无法播放音效: " + soundName + " - " + e.message);
    }
}

// 导出
if (typeof module !== 'undefined' && module.exports) {
    module.exports = { handlePlayerJoin };
}
