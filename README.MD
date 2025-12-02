# NekoKJS

[![License: LGPL v3](https://img.shields.io/badge/License-LGPL%20v3-blue.svg)](LICENSE)

一个基于 Leaves 服务端的 JavaScript 脚本插件，受 [KubeJS](https://github.com/KubeJS-Mods/KubeJS) 启发，为 Minecraft 服务器带来强大的脚本化能力。

## 特性

- 🚀 **KubeJS 风格** - 使用 KubeJS 专用的 Rhino 引擎
- 📦 **模块化设计** - 支持脚本包系统，可以使用 `load()` / `require()` 加载模块
- 🎯 **事件驱动** - 反射式事件监听，支持所有 Bukkit 事件
- 🌍 **多语言支持** - 内置简体中文和英文双语支持
- 🔄 **热重载** - 支持运行时重新加载脚本
- 🛠️ **丰富的 API** - Console、Server、Events 等开箱即用的 API

## 依赖要求

- **服务端**: [Leaves](https://github.com/LeavesMC/Leaves) 1.21.8+
- **JDK**: 21 或更高版本
- **Gradle**: 8.0+

## 快速开始

### 1. 构建插件

```bash
./gradlew build
```

构建成功后，插件 JAR 文件会生成在 `build/libs` 目录下。

### 2. 安装插件

将生成的 `.jar` 文件放入 Leaves 服务器的 `plugins` 文件夹，重启服务器。

### 3. 创建脚本包

插件会自动在 `plugins/NekoKJS/resources/` 目录下创建示例脚本包：

```
plugins/NekoKJS/resources/
└── example_pack/              # 脚本包
    ├── pack.yml              # 包配置
    └── data/                 # 脚本目录
        ├── main.js          # 入口文件 ⭐
        ├── config.js        # 配置模块
        ├── utils/
        │   └── helper.js    # 工具函数
        └── events/
            ├── player.js    # 玩家事件
            └── world.js     # 世界事件
```

### 4. 编写脚本

创建你自己的脚本包：

**pack.yml**
```yaml
pack:
  name: "My Script Pack"
  namespace: "my_pack"
  version: "1.0.0"
  authors:
    - "YourName"

loading:
  enabled: true
  priority: 100

scripts:
  entry: "data/main.js"  # 入口文件路径
```

**data/main.js**
```javascript
console.info("我的脚本包已加载！");

// 监听玩家加入事件
Events.playerJoin(event => {
    let player = event.getPlayer();
    player.sendMessage("§a欢迎来到服务器！");
});

// 加载其他模块
load("data/utils/helper.js");
```

## 核心功能

### 事件系统

```javascript
// 玩家事件
Events.playerJoin(event => { /* ... */ });
Events.playerQuit(event => { /* ... */ });
Events.playerChat(event => { /* ... */ });

// 世界事件
Events.blockBreak(event => { /* ... */ });
Events.blockPlace(event => { /* ... */ });

// 服务器 Tick
Events.serverTick(() => { /* ... */ });

// 通用事件（使用完整类名）
Events.on("org.bukkit.event.player.PlayerMoveEvent", event => {
    // ...
});
```

### 模块加载

```javascript
// 加载模块（两种方式）
load("data/utils/helper.js");
require("data/config.js");

// 模块在同一作用域中运行，可以共享变量和函数
```

### API

```javascript
// Console API
console.info("信息");
console.warn("警告");
console.error("错误");

// Server API
Server.getName();
Server.getVersion();
Server.getOnlinePlayerCount();
Server.broadcastMessage("消息");

// Events API
Events.playerJoin(callback);
Events.on("EventClassName", callback);
```

## 命令

- `/nekokjs reload` - 重新加载所有脚本
- `/nekokjs info` - 查看插件信息
- `/nekokjs help` - 显示帮助信息

## 配置

**config.yml**
```yaml
# 语言设置 (zh_CN, en_US)
language: zh_CN

# 调试模式
debug: false

# 事件设置
events:
  tick-interval: 20  # Tick 事件触发间隔
```

## 脚本包结构说明

每个脚本包都是一个独立的命名空间：

- **pack.yml** - 包的元数据和配置
- **data/** - 脚本文件目录（可自定义组织）
- **入口文件** - 在 pack.yml 中指定，作为包的加载入口

**优势：**
- ✅ 模块化组织代码
- ✅ 易于维护和扩展
- ✅ 支持多个脚本包共存
- ✅ 每个包有独立的命名空间，避免冲突

## 技术栈

- **Rhino 引擎**: [KubeJS-Mods/Rhino](https://github.com/KubeJS-Mods/Rhino)
- **Mixin**: 用于注入服务器 Tick 事件
- **Paper API**: Bukkit 事件系统和服务器 API

## 示例

查看 `src/main/resources/resources/example_pack/` 中的完整示例，包含：
- 模块化脚本组织
- 事件处理示例
- 工具函数库
- 配置管理

## 常见问题

**Q: 为什么使用 KubeJS 的 Rhino 而不是标准 Mozilla Rhino？**

A: KubeJS Rhino 针对 Minecraft 脚本进行了优化，提供更好的性能和兼容性。

**Q: 脚本会影响服务器性能吗？**

A: 合理编写的脚本不会显著影响性能。避免在高频事件（如 PlayerMoveEvent）中执行复杂逻辑。

**Q: 如何调试脚本？**

A: 使用 `console.info()` 输出日志，或在 config.yml 中启用 debug 模式查看详细信息。

**Q: 支持热重载吗？**

A: 支持！使用 `/nekokjs reload` 命令即可重新加载所有脚本。

## 贡献

欢迎通过 Issue 或 Pull Request 提交你的建议或改进！

## 许可证

本项目遵循 [GNU Lesser General Public License v3.0](LICENSE) 开源协议。

LGPL-3.0 允许你：
- 商业使用
- 修改代码
- 分发代码
- 专利授权
- 私人使用

但需要：
- 公开源码（如果修改了库本身）
- 保留许可证和版权声明
- 说明修改内容
- 使用相同许可证分发修改后的库

注意：使用本插件的服务器或其他插件**不需要**开源。

## 致谢

- [KubeJS](https://github.com/KubeJS-Mods/KubeJS) - 灵感来源
- [Leaves](https://github.com/LeavesMC/Leaves) - 服务端支持
- [Rhino](https://github.com/mozilla/rhino) - JavaScript 引擎