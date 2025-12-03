// 消息格式化工具
// Message Formatter Utility

const MessageFormatter = {
    /**
     * 格式化消息，替换占位符
     * @param {string} message - 原始消息
     * @param {Object} placeholders - 占位符映射
     * @returns {string} 格式化后的消息
     */
    format: function(message, placeholders) {
        let result = message;
        for (let key in placeholders) {
            const placeholder = "{" + key + "}";
            result = result.replace(new RegExp(placeholder, "g"), placeholders[key]);
        }
        return result;
    },
    
    /**
     * 格式化消息数组
     * @param {Array<string>} messages - 消息数组
     * @param {Object} placeholders - 占位符映射
     * @returns {Array<string>} 格式化后的消息数组
     */
    formatArray: function(messages, placeholders) {
        return messages.map(msg => this.format(msg, placeholders));
    },
    
    /**
     * 获取玩家相关的占位符
     * @param {Player} player - 玩家对象
     * @param {PlayerStorage} storage - 存储工具
     * @returns {Object} 占位符映射
     */
    getPlayerPlaceholders: function(player, storage) {
        // 获取显示名称（转换为纯文本）
        let displayName = player.getName(); // 默认使用玩家名
        try {
            const displayNameComponent = player.displayName();
            if (displayNameComponent) {
                // 使用 PlainTextComponentSerializer 转换为纯文本
                const PlainTextComponentSerializer = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
                displayName = PlainTextComponentSerializer.plainText().serialize(displayNameComponent);
            }
        } catch (e) {
            // 如果获取失败，使用默认名称
        }
        
        return {
            "player": player.getName(),
            "displayname": displayName,
            "online": Server.getOnlinePlayerCount(),
            "max": Server.getMaxPlayers(),
            "first_join": storage.getFirstJoinTime(player),
            "world": player.getWorld().getName()
        };
    }
};

// 导出
if (typeof module !== 'undefined' && module.exports) {
    module.exports = MessageFormatter;
}
