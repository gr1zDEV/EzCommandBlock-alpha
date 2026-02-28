package com.ezinnovations.ezcommandblocker;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatTabCompleteEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;

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
    public void onAsyncChatTab(AsyncPlayerChatTabCompleteEvent event) {
        final Player player = event.getPlayer();
        if (player.hasPermission("ezcommandblocker.bypass.tab")) {
            return;
        }

        final String groupName = resolveGroupName(player);
        final Set<String> allowedCommands = configManager.resolveGroupCommands(groupName);
        event.getTabCompletions().removeIf(completion -> {
            final String normalized = normalize(completion);
            if (!completion.startsWith("/")) {
                return false;
            }
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
