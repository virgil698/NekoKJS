// 玩家数据存储工具
// Player Data Storage Utility

const PlayerStorage = {
    // 存储首次加入的玩家
    firstJoinPlayers: new Set(),
    
    /**
     * 检查玩家是否首次加入
     * @param {Player} player - 玩家对象
     * @returns {boolean} 是否首次加入
     */
    isFirstJoin: function(player) {
        // 检查玩家是否曾经玩过（通过游戏时间判断）
        return player.getStatistic(org.bukkit.Statistic.PLAY_ONE_MINUTE) === 0;
    },
    
    /**
     * 标记玩家已经加入过
     * @param {Player} player - 玩家对象
     */
    markJoined: function(player) {
        this.firstJoinPlayers.add(player.getUniqueId().toString());
    },
    
    /**
     * 获取玩家首次加入时间（格式化）
     * @param {Player} player - 玩家对象
     * @returns {string} 格式化的时间
     */
    getFirstJoinTime: function(player) {
        const firstPlayed = player.getFirstPlayed();
        if (firstPlayed === 0) {
            return "刚刚";
        }
        
        const now = Date.now();
        const diff = now - firstPlayed;
        
        const days = Math.floor(diff / (1000 * 60 * 60 * 24));
        if (days > 0) {
            return days + " 天前";
        }
        
        const hours = Math.floor(diff / (1000 * 60 * 60));
        if (hours > 0) {
            return hours + " 小时前";
        }
        
        const minutes = Math.floor(diff / (1000 * 60));
        if (minutes > 0) {
            return minutes + " 分钟前";
        }
        
        return "刚刚";
    }
};

// 导出
if (typeof module !== 'undefined' && module.exports) {
    module.exports = PlayerStorage;
}
