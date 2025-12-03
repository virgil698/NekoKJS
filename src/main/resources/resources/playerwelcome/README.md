# 玩家欢迎消息包 (Player Welcome Pack)

一个完整的玩家欢迎消息系统，提供丰富的自定义选项。

## 功能特性

### 🎉 欢迎消息
- 支持 MiniMessage 格式的彩色消息
- 可自定义多行欢迎消息
- 支持占位符替换（玩家名、在线人数等）
- 可配置延迟发送

### 🏆 标题消息
- 支持主标题和副标题
- 可自定义淡入、停留、淡出时间
- 支持渐变色和特效

### 🌟 首次加入特殊待遇
- 检测玩家是否首次加入
- 全服广播新玩家加入
- 特殊的欢迎消息和标题
- 独特的音效提示

### 🔊 音效系统
- 玩家加入时播放音效
- 首次加入播放特殊音效
- 可自定义音量和音调

### 👋 退出消息
- 全服广播玩家退出消息
- 支持自定义格式

## 文件结构

```
playerwelcome/
├── pack.yml                    # 包配置文件
├── README.md                   # 说明文档
└── data/
    ├── main.js                 # 主入口文件
    ├── config.js               # 配置文件
    ├── utils/                  # 工具模块
    │   ├── storage.js          # 数据存储
    │   └── formatter.js        # 消息格式化
    └── handlers/               # 事件处理器
        ├── join.js             # 加入处理
        └── quit.js             # 退出处理
```

## 配置说明

### 基础配置

在 `data/config.js` 中修改配置：

```javascript
const WelcomeConfig = {
    enabled: true,  // 是否启用整个系统
    // ... 其他配置
};
```

### 聊天消息配置

```javascript
chatMessages: {
    enabled: true,
    messages: [
        "<gold><bold>━━━━━━━━━━━━━━━━━━━━━━━━",
        "<gradient:#FFD700:#FFA500><bold>  欢迎来到服务器！</gradient>",
        // ... 更多消息
    ],
    delay: 10  // 延迟发送（tick）
}
```

### 标题消息配置

```javascript
titleMessage: {
    enabled: true,
    title: "<gradient:#00FF00:#00FFFF><bold>欢迎回来！</gradient>",
    subtitle: "<gray>祝你游戏愉快 {player}",
    fadeIn: 10,   // 淡入时间
    stay: 60,     // 停留时间
    fadeOut: 20,  // 淡出时间
    delay: 5      // 延迟发送
}
```

### 首次加入配置

```javascript
firstJoin: {
    enabled: true,
    broadcast: "<yellow>欢迎新玩家 <bold>{player}</bold> 首次加入服务器！🎉",
    messages: [
        // 给新玩家的特殊消息
    ],
    title: "<gradient:#FF1493:#FFD700><bold>欢迎！</gradient>",
    subtitle: "<green>开始你的冒险之旅"
}
```

### 音效配置

```javascript
sounds: {
    enabled: true,
    joinSound: "ENTITY_PLAYER_LEVELUP",
    firstJoinSound: "UI_TOAST_CHALLENGE_COMPLETE",
    volume: 1.0,
    pitch: 1.0
}
```

## 占位符列表

可在消息中使用以下占位符：

- `{player}` - 玩家名称
- `{displayname}` - 玩家显示名称
- `{online}` - 当前在线人数
- `{max}` - 最大玩家数
- `{first_join}` - 首次加入时间（相对时间）
- `{world}` - 玩家所在世界

## 使用示例

### 自定义欢迎消息

编辑 `data/config.js`：

```javascript
chatMessages: {
    enabled: true,
    messages: [
        "<rainbow>欢迎 {player} 来到我们的服务器！",
        "<green>当前有 {online} 位玩家在线",
        "<yellow>输入 /help 查看帮助"
    ],
    delay: 20  // 延迟 1 秒发送
}
```

### 禁用某个功能

```javascript
titleMessage: {
    enabled: false,  // 禁用标题消息
    // ... 其他配置
}
```

### 修改音效

```javascript
sounds: {
    enabled: true,
    joinSound: "BLOCK_NOTE_BLOCK_PLING",  // 更换为音符盒音效
    volume: 0.5,  // 降低音量
    pitch: 1.5    // 提高音调
}
```

## MiniMessage 格式参考

### 颜色
- `<red>红色</red>`
- `<green>绿色</green>`
- `<blue>蓝色</blue>`
- `<yellow>黄色</yellow>`
- `<gold>金色</gold>`
- `<aqua>青色</aqua>`
- `<white>白色</white>`
- `<gray>灰色</gray>`

### 格式
- `<bold>粗体</bold>`
- `<italic>斜体</italic>`
- `<underlined>下划线</underlined>`
- `<strikethrough>删除线</strikethrough>`

### 渐变色
- `<gradient:#FF0000:#00FF00>渐变文本</gradient>`
- `<gradient:#FFD700:#FFA500:#FF69B4>多色渐变</gradient>`

### 彩虹色
- `<rainbow>彩虹文本</rainbow>`

## 常见音效列表

- `ENTITY_PLAYER_LEVELUP` - 升级音效
- `UI_TOAST_CHALLENGE_COMPLETE` - 挑战完成
- `BLOCK_NOTE_BLOCK_PLING` - 音符盒
- `ENTITY_EXPERIENCE_ORB_PICKUP` - 经验球拾取
- `BLOCK_BELL_USE` - 钟声
- `ENTITY_VILLAGER_YES` - 村民同意

## 技术说明

### 模块化设计
- **config.js** - 集中管理所有配置
- **storage.js** - 处理玩家数据存储和首次加入检测
- **formatter.js** - 消息格式化和占位符替换
- **join.js** - 玩家加入事件处理逻辑
- **quit.js** - 玩家退出事件处理逻辑
- **main.js** - 主入口，负责加载和初始化

### 首次加入检测
使用 Bukkit 的 `PLAY_ONE_MINUTE` 统计数据来判断玩家是否首次加入服务器。

### 延迟发送
使用 `Server.runCommandLater()` 实现延迟发送，确保玩家完全加载后再显示消息。

## 许可证

本包是 NekoKJS 项目的一部分，遵循项目的开源许可证。

## 作者

NekoKJS Team

---

**提示**: 修改配置后需要重新加载脚本包才能生效。使用命令 `/nekokjs reload` 重新加载所有脚本包。
