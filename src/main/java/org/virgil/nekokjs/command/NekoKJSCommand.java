package org.virgil.nekokjs.command;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;
import org.virgil.nekokjs.NekoKJSPlugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * NekoKJS 主命令
 * 使用 Paper 命令 API
 * 用法: /nekokjs <reload|info|help>
 * 重载子命令: /nekokjs reload <all|config|dataconfig>
 */
public class NekoKJSCommand implements BasicCommand {
    private final NekoKJSPlugin plugin;
    private final MiniMessage miniMessage;

    public NekoKJSCommand(NekoKJSPlugin plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        // 检查基本权限
        if (!stack.getSender().hasPermission("nekokjs.command.use")) {
            sendMessage(stack, "command.no-permission");
            return;
        }
        
        if (args.length == 0) {
            sendHelp(stack);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                if (!stack.getSender().hasPermission("nekokjs.command.reload")) {
                    sendMessage(stack, "command.no-permission");
                    return;
                }
                
                // 获取重载类型
                String reloadType = args.length > 1 ? args[1].toLowerCase() : "all";
                
                sendMessage(stack, "command.reload.start");
                long startTime = System.currentTimeMillis();
                
                try {
                    switch (reloadType) {
                        case "all" -> {
                            // 重载全部配置
                            plugin.getConfigManager().reloadConfig();
                            plugin.getScriptManager().reloadScriptConfigs();
                            sendMessage(stack, "command.reload.all-success", "time", System.currentTimeMillis() - startTime);
                        }
                        case "config" -> {
                            // 重载插件配置
                            plugin.getConfigManager().reloadConfig();
                            sendMessage(stack, "command.reload.config-success", "time", System.currentTimeMillis() - startTime);
                        }
                        case "dataconfig" -> {
                            // 重载脚本配置
                            plugin.getScriptManager().reloadScriptConfigs();
                            sendMessage(stack, "command.reload.dataconfig-success", "time", System.currentTimeMillis() - startTime);
                        }
                        default -> sendMessage(stack, "command.reload.unknown-type");
                    }
                } catch (Exception e) {
                    sendMessage(stack, "command.reload.failed", "error", e.getMessage());
                    e.printStackTrace();
                }
            }
            
            case "info" -> {
                sendMessage(stack, "info.header");
                sendMessage(stack, "info.version", "version", plugin.getPluginMeta().getVersion());
                sendMessage(stack, "info.authors", "authors", String.join(", ", plugin.getPluginMeta().getAuthors()));
                sendMessage(stack, "info.scripts-dir", "dir", plugin.getScriptManager().getScriptsDir().getAbsolutePath());
                sendMessage(stack, "info.tick-count", "count", plugin.getEventManager().getTickCount());
                sendMessage(stack, "info.footer");
            }
            
            case "list" -> {
                if (args.length == 1) {
                    // 列出所有脚本包
                    sendMessage(stack, "list.header");
                    var packs = plugin.getScriptManager().getScriptPacks();
                    if (packs.isEmpty()) {
                        sendMessage(stack, "list.no-packs");
                    } else {
                        for (var pack : packs) {
                            String status = plugin.getConfigManager().getMessage(
                                pack.isEnabled() ? "list.status-enabled" : "list.status-disabled"
                            );
                            sendMessage(stack, "list.pack-item", 
                                "name", pack.getName(),
                                "namespace", pack.getNamespace(),
                                "version", pack.getVersion(),
                                "status", status
                            );
                        }
                        sendMessage(stack, "list.footer", "count", packs.size());
                    }
                } else {
                    // 显示特定脚本包的详细信息
                    String packName = args[1];
                    var pack = plugin.getScriptManager().getScriptPack(packName);
                    if (pack == null) {
                        sendMessage(stack, "list.pack-not-found", "name", packName);
                    } else {
                        sendMessage(stack, "list.detail-header", "name", pack.getName());
                        sendMessage(stack, "list.detail-namespace", "namespace", pack.getNamespace());
                        sendMessage(stack, "list.detail-version", "version", pack.getVersion());
                        sendMessage(stack, "list.detail-authors", "authors", String.join(", ", pack.getAuthors()));
                        sendMessage(stack, "list.detail-description", "description", pack.getDescription());
                        sendMessage(stack, "list.detail-entry", "entry", pack.getEntryPoint());
                        sendMessage(stack, "list.detail-priority", "priority", pack.getPriority());
                        String enabled = plugin.getConfigManager().getMessage(
                            pack.isEnabled() ? "list.enabled-yes" : "list.enabled-no"
                        );
                        sendMessage(stack, "list.detail-enabled", "enabled", enabled);
                        sendMessage(stack, "list.detail-footer");
                    }
                }
            }
            
            case "help" -> sendHelp(stack);
            
            default -> sendMessage(stack, "command.unknown");
        }
    }

    private void sendHelp(CommandSourceStack stack) {
        sendMessage(stack, "help.header");
        sendMessage(stack, "help.reload");
        sendMessage(stack, "help.list");
        sendMessage(stack, "help.info");
        sendMessage(stack, "help.help");
        sendMessage(stack, "help.footer");
    }

    /**
     * 发送翻译消息
     * 使用 MiniMessage 解析颜色和格式
     */
    private void sendMessage(CommandSourceStack stack, String key, Object... replacements) {
        String message = plugin.getConfigManager().getMessage(key, replacements);
        Component component = miniMessage.deserialize(message);
        stack.getSender().sendMessage(component);
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        List<String> suggestions = new ArrayList<>();
        
        if (args.length <= 1) {
            // 主命令建议
            suggestions.add("reload");
            suggestions.add("list");
            suggestions.add("info");
            suggestions.add("help");
            
            // 过滤匹配的选项
            if (args.length == 1) {
                String input = args[0].toLowerCase();
                return suggestions.stream()
                        .filter(s -> s.toLowerCase().startsWith(input))
                        .toList();
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("reload")) {
                // reload 子命令建议
                suggestions.add("all");
                suggestions.add("config");
                suggestions.add("dataconfig");
            } else if (args[0].equalsIgnoreCase("list")) {
                // list 子命令建议 - 所有脚本包名称
                return plugin.getScriptManager().getScriptPacks().stream()
                        .map(pack -> pack.getNamespace())
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .toList();
            }
            
            String input = args[1].toLowerCase();
            return suggestions.stream()
                    .filter(s -> s.toLowerCase().startsWith(input))
                    .toList();
        }
        
        return suggestions;
    }
}
