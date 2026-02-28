package com.ezinnovations.ezcommandblocker;

import com.ezinnovations.ezcommandblocker.util.ColorUtil;
import com.ezinnovations.ezcommandblocker.util.FoliaUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Locale;

public final class ActionExecutor {
    private final Plugin plugin;

    public ActionExecutor(Plugin plugin) {
        this.plugin = plugin;
    }

    public void execute(Player player, List<String> actions) {
        for (String rawAction : actions) {
            if (rawAction == null || rawAction.isBlank()) {
                continue;
            }

            final String[] parts = rawAction.split(":", 2);
            if (parts.length < 2) {
                continue;
            }

            final String type = parts[0].trim().toLowerCase(Locale.ROOT);
            final String value = parts[1].trim().replace("%player%", player.getName());

            switch (type) {
                case "message" -> player.sendMessage(ColorUtil.colorize(value));
                case "console_command" -> FoliaUtil.runTask(plugin, player,
                        () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), stripSlash(value)));
                case "player_command" -> FoliaUtil.runTask(plugin, player,
                        () -> player.performCommand(stripSlash(value)));
                case "kick" -> FoliaUtil.runTask(plugin, player, () -> player.kickPlayer(ColorUtil.colorize(value)));
                default -> plugin.getLogger().warning("Unknown action type: " + type);
            }
        }
    }

    private String stripSlash(String command) {
        return command.startsWith("/") ? command.substring(1) : command;
    }
}
