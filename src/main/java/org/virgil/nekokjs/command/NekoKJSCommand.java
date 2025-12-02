package org.virgil.nekokjs.command;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.virgil.nekokjs.NekoKJSPlugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * NekoKJS 主命令
 * 使用 Paper 命令 API
 * 用法: /nekokjs <reload|info|help>
 */
public class NekoKJSCommand implements BasicCommand {
    private final NekoKJSPlugin plugin;

    public NekoKJSCommand(NekoKJSPlugin plugin) {
        this.plugin = plugin;
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
                
                sendMessage(stack, "command.reload.start");
                long startTime = System.currentTimeMillis();
                
                try {
                    plugin.getScriptManager().reloadAllScripts();
                    long duration = System.currentTimeMillis() - startTime;
                    sendMessage(stack, "command.reload.success", "time", duration);
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
            
            case "help" -> sendHelp(stack);
            
            default -> sendMessage(stack, "command.unknown");
        }
    }

    private void sendHelp(CommandSourceStack stack) {
        sendMessage(stack, "help.header");
        sendMessage(stack, "help.reload");
        sendMessage(stack, "help.info");
        sendMessage(stack, "help.help");
        sendMessage(stack, "help.footer");
    }

    /**
     * 发送翻译消息
     */
    private void sendMessage(CommandSourceStack stack, String key, Object... replacements) {
        String message = plugin.getConfigManager().getMessage(key, replacements);
        stack.getSender().sendMessage(Component.text(message));
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        List<String> suggestions = new ArrayList<>();
        
        if (args.length <= 1) {
            suggestions.add("reload");
            suggestions.add("info");
            suggestions.add("help");
            
            // 过滤匹配的选项
            if (args.length == 1) {
                String input = args[0].toLowerCase();
                return suggestions.stream()
                        .filter(s -> s.toLowerCase().startsWith(input))
                        .toList();
            }
        }
        
        return suggestions;
    }
}
