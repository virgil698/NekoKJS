// 时间 API 使用示例
// Time API Examples

console.info("=== 时间 API 示例 / Time API Examples ===");

// ===== 游戏内时间 =====
// In-game Time

// 获取当前世界时间（0-24000）
let worldTime = Server.getWorldTime();
console.info("游戏内时间 / World Time: " + worldTime);

// 时间转换为可读格式
function formatGameTime(time) {
    let hours = Math.floor(time / 1000) + 6; // Minecraft 时间从 6:00 开始
    if (hours >= 24) hours -= 24;
    let minutes = Math.floor((time % 1000) / 1000 * 60);
    return hours + ":" + (minutes < 10 ? "0" : "") + minutes;
}

console.info("格式化时间 / Formatted Time: " + formatGameTime(worldTime));

// 判断是白天还是晚上
function isDaytime(time) {
    return time >= 0 && time < 13000;
}

if (isDaytime(worldTime)) {
    console.info("当前是白天 / It's daytime");
} else {
    console.info("当前是晚上 / It's nighttime");
}

// ===== 系统时间 =====
// System Time

// 获取系统时间戳（毫秒）
let timestamp = Server.getCurrentTimeMillis();
console.info("系统时间戳 / System Timestamp: " + timestamp);

// 转换为日期（使用 Java Date）
let date = new java.util.Date(timestamp);
console.info("当前日期 / Current Date: " + date.toString());

// ===== 游戏刻数 =====
// Game Ticks

// 获取游戏刻数
let gameTicks = Server.getGameTime();
console.info("游戏刻数 / Game Ticks: " + gameTicks);

// 计算游戏运行时间（秒）
let gameSeconds = gameTicks / 20; // 20 ticks = 1 second
console.info("游戏运行时间 / Game Runtime: " + gameSeconds + " 秒/seconds");

// ===== 完整时间 =====
// Full Time

// 获取完整时间（包含天数）
let fullTime = Server.getFullTime();
console.info("完整时间 / Full Time: " + fullTime);

// 计算游戏天数
let days = Math.floor(fullTime / 24000);
console.info("游戏天数 / Game Days: " + days);

// ===== 时间控制示例 =====
// Time Control Examples

// 设置为白天（1000 = 早上 7:00）
// Server.setWorldTime(1000);

// 设置为中午（6000 = 中午 12:00）
// Server.setWorldTime(6000);

// 设置为晚上（13000 = 晚上 7:00）
// Server.setWorldTime(13000);

// 设置为午夜（18000 = 午夜 0:00）
// Server.setWorldTime(18000);

// ===== 实用函数示例 =====
// Utility Functions

/**
 * 设置为白天
 */
function setDay() {
    Server.setWorldTime(1000);
    Message.broadcast("<yellow>☀ 时间已设置为白天！/ Time set to day!");
}

/**
 * 设置为晚上
 */
function setNight() {
    Server.setWorldTime(13000);
    Message.broadcast("<blue>🌙 时间已设置为晚上！/ Time set to night!");
}

/**
 * 获取时间描述
 */
function getTimeDescription() {
    let time = Server.getWorldTime();
    
    if (time >= 0 && time < 6000) {
        return "<yellow>早晨 / Morning";
    } else if (time >= 6000 && time < 12000) {
        return "<gold>中午 / Noon";
    } else if (time >= 12000 && time < 13000) {
        return "<orange>傍晚 / Evening";
    } else if (time >= 13000 && time < 18000) {
        return "<blue>夜晚 / Night";
    } else {
        return "<dark_blue>深夜 / Midnight";
    }
}

// ===== 定时任务示例 =====
// Scheduled Task Examples

// 每游戏日自动问候
let lastDay = -1;

Events.serverTick(() => {
    let currentDay = Math.floor(Server.getFullTime() / 24000);
    
    if (currentDay > lastDay) {
        lastDay = currentDay;
        Message.broadcast("<gold>新的一天开始了！/ A new day has begun! <gray>(Day " + currentDay + ")");
    }
});

// 在特定时间触发事件
Events.serverTick(() => {
    let time = Server.getWorldTime();
    
    // 每天早上 6:00（时间 = 0）
    if (time === 0) {
        Message.broadcast("<yellow>☀ 早安！新的一天开始了！/ Good morning!");
    }
    
    // 每天晚上 7:00（时间 = 13000）
    if (time === 13000) {
        Message.broadcast("<blue>🌙 晚安！夜幕降临了！/ Good night!");
    }
});

console.info("=== 时间 API 示例加载完成 / Time API Examples Loaded ===");
