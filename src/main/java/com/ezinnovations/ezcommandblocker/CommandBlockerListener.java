package com.ezinnovations.ezcommandblocker;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;
import java.util.Map;

public final class CommandBlockerListener implements Listener {
    private final ConfigManager configManager;
    private final ActionExecutor actionExecutor;

    public CommandBlockerListener(ConfigManager configManager, ActionExecutor actionExecutor) {
        this.configManager = configManager;
        this.actionExecutor = actionExecutor;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        final Player player = event.getPlayer();
        if (player.hasPermission("ezcommandblocker.bypass")) {
            return;
        }

        final String baseCommand = ConfigManager.normalizeCommand(event.getMessage());
        if (baseCommand.isBlank()) {
            return;
        }

        final boolean containsColon = baseCommand.contains(":");
        final boolean listed = configManager.getCommandSet().contains(baseCommand);

        final boolean blocked = (configManager.isBlockColonCommands() && containsColon)
                || (configManager.isUseCommandsAsWhitelist() ? !listed : listed);

        if (!blocked) {
            return;
        }

        event.setCancelled(true);

        final List<String> actions = findCustomActions(baseCommand, configManager.getCustomActionGroups());
        actionExecutor.execute(player, actions != null ? actions : configManager.getDefaultActions());
    }

    private List<String> findCustomActions(String command, Map<String, ConfigManager.CustomActionGroup> groups) {
        for (ConfigManager.CustomActionGroup group : groups.values()) {
            if (group.commands().contains(command)) {
                return group.actions();
            }
        }
        return null;
    }
}
