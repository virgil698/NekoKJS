// 玩家欢迎消息配置文件
// Player Welcome Message Configuration

const WelcomeConfig = {
    // 是否启用欢迎消息
    enabled: true,
    
    // 聊天消息配置
    chatMessages: {
        enabled: true,
        // 欢迎消息列表（支持 MiniMessage 格式）
        messages: [
            "<gold><bold>━━━━━━━━━━━━━━━━━━━━━━━━",
            "<gradient:#FFD700:#FFA500><bold>  欢迎来到服务器！</gradient>",
            "<yellow>  玩家: <white>{player}",
            "<yellow>  当前在线: <white>{online} <yellow>人",
            "<gray>  首次加入: <white>{first_join}",
            "<gold><bold>━━━━━━━━━━━━━━━━━━━━━━━━"
        ],
        // 延迟发送（tick，20 tick = 1秒）
        delay: 10
    },
    
    // 标题消息配置
    titleMessage: {
        enabled: true,
        // 主标题
        title: "<gradient:#00FF00:#00FFFF><bold>欢迎回来！</gradient>",
        // 副标题
        subtitle: "<gray>祝你游戏愉快 {player}",
        // 淡入时间（tick）
        fadeIn: 10,
        // 停留时间（tick）
        stay: 60,
        // 淡出时间（tick）
        fadeOut: 20,
        // 延迟发送（tick）
        delay: 5
    },
    
    // 首次加入特殊消息
    firstJoin: {
        enabled: true,
        // 全服广播消息
        broadcast: "<yellow>欢迎新玩家 <gradient:#FFD700:#FFA500><bold>{player}</bold></gradient> <yellow>首次加入服务器！🎉",
        // 给新玩家的特殊消息
        messages: [
            "",
            "<gradient:#FF69B4:#FFD700><bold>  🌟 欢迎新玩家！🌟</gradient>",
            "",
            "<green>  这是你第一次来到我们的服务器",
            "<aqua>  输入 <white>/help <aqua>查看帮助",
            "<aqua>  输入 <white>/spawn <aqua>返回出生点",
            ""
        ],
        // 首次加入标题
        title: "<gradient:#FF1493:#FFD700><bold>欢迎！</gradient>",
        subtitle: "<green>开始你的冒险之旅",
        // 标题时间设置
        fadeIn: 20,
        stay: 80,
        fadeOut: 20
    },
    
    // 退出消息配置
    quitMessage: {
        enabled: true,
        // 全服广播消息
        broadcast: "<gray>玩家 <white>{player} <gray>离开了游戏"
    },
    
    // 音效配置
    sounds: {
        enabled: true,
        // 加入时播放的音效
        joinSound: "ENTITY_PLAYER_LEVELUP",
        // 首次加入时播放的音效
        firstJoinSound: "UI_TOAST_CHALLENGE_COMPLETE",
        // 音量
        volume: 1.0,
        // 音调
        pitch: 1.0
    }
};

// 导出配置
if (typeof module !== 'undefined' && module.exports) {
    module.exports = WelcomeConfig;
}
