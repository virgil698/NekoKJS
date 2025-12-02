// 世界事件处理模块
// World Events Module

console.info("[WorldEvents] 世界事件模块已加载 / World events module loaded");

// 方块破坏事件
Events.blockBreak(event => {
    let player = event.getPlayer();
    let block = event.getBlock();
    let type = block.getType().toString();
    
    console.info(formatPlayerName(player, "§a") + " §7破坏了 §f" + type);
    
    // 钻石矿石特殊处理
    if (type === "DIAMOND_ORE" || type === "DEEPSLATE_DIAMOND_ORE") {
        broadcastColored("🎉 " + player.getName() + " 挖到了钻石！", "§b");
    }
    
    // 远古残骸特殊处理
    if (type === "ANCIENT_DEBRIS") {
        broadcastColored("⚡ " + player.getName() + " 发现了远古残骸！", "§5");
    }
});

// 方块放置事件
Events.blockPlace(event => {
    let player = event.getPlayer();
    let block = event.getBlock();
    let type = block.getType().toString();
    
    // 记录特殊方块的放置
    if (type === "TNT" || type === "SPAWNER") {
        console.info("§c[警告] " + player.getName() + " 放置了 " + type);
    }
});

// 实体死亡事件
Events.entityDeath(event => {
    let entity = event.getEntity();
    let type = entity.getType().toString();
    
    // Boss 击杀广播
    if (type === "ENDER_DRAGON") {
        let killer = entity.getKiller();
        if (killer != null) {
            broadcastColored("🐉 " + killer.getName() + " 击败了末影龙！", "§5");
        }
    }
    
    if (type === "WITHER") {
        let killer = entity.getKiller();
        if (killer != null) {
            broadcastColored("💀 " + killer.getName() + " 击败了凋零！", "§c");
        }
    }
});

console.info("[WorldEvents] 已注册 3 个世界事件 / 3 world events registered");
