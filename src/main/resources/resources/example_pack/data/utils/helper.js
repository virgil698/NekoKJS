// 工具函数模块
// Utility Functions Module

console.info("[Helper] 工具模块已加载 / Helper module loaded");

/**
 * 格式化玩家名称
 * Format player name with color
 */
function formatPlayerName(player, color) {
    if (!color) color = "§e";
    return color + player.getName() + "§r";
}

/**
 * 广播彩色消息
 * Broadcast colored message
 */
function broadcastColored(message, color) {
    if (!color) color = "§a";
    Server.broadcastMessage(color + message);
}

/**
 * 给予玩家物品（示例）
 * Give item to player (example)
 */
function giveItem(player, itemType, amount) {
    player.sendMessage("§a已获得 " + amount + " 个 " + itemType);
    // 实际给予物品的代码需要使用 Bukkit API
}

/**
 * 检查玩家是否有权限
 * Check if player has permission
 */
function hasPermission(player, permission) {
    return player.hasPermission(permission);
}

/**
 * 计算两点之间的距离
 * Calculate distance between two locations
 */
function distance(loc1, loc2) {
    let dx = loc1.getX() - loc2.getX();
    let dy = loc1.getY() - loc2.getY();
    let dz = loc1.getZ() - loc2.getZ();
    return Math.sqrt(dx * dx + dy * dy + dz * dz);
}

console.info("[Helper] 已定义 5 个工具函数 / 5 utility functions defined");
