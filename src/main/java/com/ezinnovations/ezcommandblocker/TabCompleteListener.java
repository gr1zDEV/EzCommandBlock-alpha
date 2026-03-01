package com.ezinnovations.ezcommandblocker;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.command.CommandSender;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.server.TabCompleteEvent;

import java.util.Locale;
import java.util.Set;

public final class TabCompleteListener implements Listener {
    private final ConfigManager configManager;

    public TabCompleteListener(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onCommandSend(PlayerCommandSendEvent event) {
        final Player player = event.getPlayer();
        if (player.hasPermission("ezcommandblocker.bypass.tab")) {
            return;
        }

        final String groupName = resolveGroupName(player);
        final Set<String> allowedCommands = configManager.resolveGroupCommands(groupName);
        event.getCommands().removeIf(command -> !allowedCommands.contains(normalize(command)));
    }

    @EventHandler(ignoreCancelled = true)
    public void onTabComplete(TabCompleteEvent event) {
        final CommandSender sender = event.getSender();
        if (!(sender instanceof Player player)) {
            return;
        }

        if (player.hasPermission("ezcommandblocker.bypass.tab")) {
            return;
        }

        final String buffer = event.getBuffer();
        if (!buffer.startsWith("/")) {
            return;
        }

        if (buffer.trim().contains(" ")) {
            return;
        }

        final String groupName = resolveGroupName(player);
        final Set<String> allowedCommands = configManager.resolveGroupCommands(groupName);
        event.getCompletions().removeIf(completion -> {
            final String normalized = normalize(completion);
            return !allowedCommands.contains(normalized);
        });
    }

    private String resolveGroupName(Player player) {
        return configManager.getTabGroups().entrySet().stream()
                .filter(entry -> player.hasPermission("ezcommandblocker.tab." + entry.getKey()))
                .max((left, right) -> Integer.compare(left.getValue().priority(), right.getValue().priority()))
                .map(entry -> entry.getKey().toLowerCase(Locale.ROOT))
                .orElse("default");
    }

    private String normalize(String command) {
        return ConfigManager.normalizeCommand(command);
    }
}
