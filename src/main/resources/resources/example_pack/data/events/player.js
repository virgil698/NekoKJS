// 玩家事件处理模块
// Player Events Module

console.info("[PlayerEvents] 玩家事件模块已加载 / Player events module loaded");

// 注册玩家加入事件
Events.playerJoin(event => {
    let player = event.getPlayer();
    let playerName = player.getName();
    
    console.info("<aqua>" + playerName + " <gray>加入了服务器");
    
    // 欢迎消息（使用 MiniMessage 格式）
    // 推荐使用 Message.send() 发送富文本消息
    Message.send(player, "<gold><bold>━━━━━━━━━━━━━━━━━━━━━━━━");
    Message.send(player, "<green><bold>  欢迎来到服务器！");
    Message.send(player, "<yellow>  当前在线: <white>" + Server.getOnlinePlayerCount() + " <yellow>人");
    Message.send(player, "<gold><bold>━━━━━━━━━━━━━━━━━━━━━━━━");
    
    // 如果需要发送普通文本消息，可以使用 PlayerMessageHelper 避免方法歧义：
    // PlayerMessageHelper.sendMessage(player, "普通文本消息");
    
    // 广播加入消息
    Message.broadcast("<yellow>玩家 <white>" + player.getName() + " <yellow>加入了游戏！");
});

// 注册玩家退出事件
Events.playerQuit(event => {
    let player = event.getPlayer();
    
    console.info("<red>" + player.getName() + " <gray>离开了服务器");
    Message.broadcast("<gray>玩家 <white>" + player.getName() + " <gray>离开了游戏！");
});

// 注册玩家聊天事件
Events.playerChat(event => {
    let player = event.getPlayer();
    let message = event.getMessage();
    
    console.info("[聊天] " + player.getName() + ": " + message);
    
    // 检测特殊关键词
    if (message.toLowerCase().includes("hello")) {
        Message.send(player, "<green>Hello, <white>" + player.getName() + "<green>! 👋");
    }
    
    if (message.toLowerCase().includes("help")) {
        Message.send(player, "<yellow>需要帮助吗？输入 <white>/help <yellow>查看命令列表");
    }
});

console.info("[PlayerEvents] 已注册 3 个玩家事件 / 3 player events registered");
