// 玩家事件处理模块
// Player Events Module

console.info("[PlayerEvents] 玩家事件模块已加载 / Player events module loaded");

// 注册玩家加入事件
Events.playerJoin(event => {
    let player = event.getPlayer();
    let playerName = formatPlayerName(player, "§b"); // 使用来自 helper.js 的函数
    
    console.info(playerName + " §7加入了服务器");
    
    // 欢迎消息
    player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━");
    player.sendMessage("§a§l  欢迎来到服务器！");
    player.sendMessage("§e  当前在线: §f" + Server.getOnlinePlayerCount() + " §e人");
    player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━");
    
    // 广播加入消息
    broadcastColored("玩家 " + player.getName() + " 加入了游戏！", "§e");
});

// 注册玩家退出事件
Events.playerQuit(event => {
    let player = event.getPlayer();
    let playerName = formatPlayerName(player, "§c");
    
    console.info(playerName + " §7离开了服务器");
    broadcastColored("玩家 " + player.getName() + " 离开了游戏！", "§7");
});

// 注册玩家聊天事件
Events.playerChat(event => {
    let player = event.getPlayer();
    let message = event.getMessage();
    
    console.info("[聊天] " + player.getName() + ": " + message);
    
    // 检测特殊关键词
    if (message.toLowerCase().includes("hello")) {
        player.sendMessage("§aHello, " + player.getName() + "! 👋");
    }
    
    if (message.toLowerCase().includes("help")) {
        player.sendMessage("§e需要帮助吗？输入 /help 查看命令列表");
    }
});

console.info("[PlayerEvents] 已注册 3 个玩家事件 / 3 player events registered");
