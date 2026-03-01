package com.ezinnovations.ezcommandblocker.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.player.TabCompleteEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyReloadEvent;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;

public final class EzCommandBlockerVelocity {
    private static final String BYPASS_PERMISSION = "ezcommandblocker.bypass";

    private final Logger logger;
    private final VelocityConfigManager configManager;

    @Inject
    public EzCommandBlockerVelocity(ProxyServer server,
                                    PluginContainer pluginContainer,
                                    Logger logger,
                                    @DataDirectory Path dataDirectory) {
        this.logger = logger;
        this.configManager = new VelocityConfigManager(dataDirectory);
        server.getEventManager().register(pluginContainer, this);
    }

    @Subscribe
    public void onProxyInit(ProxyInitializeEvent event) {
        reloadConfig();
    }

    @Subscribe
    public void onProxyReload(ProxyReloadEvent event) {
        reloadConfig();
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onCommandExecute(CommandExecuteEvent event) {
        if (!(event.getCommandSource() instanceof Player player)) {
            return;
        }

        if (player.hasPermission(BYPASS_PERMISSION)) {
            return;
        }

        final String baseCommand = VelocityConfigManager.normalizeCommand(event.getCommand());
        if (baseCommand.isBlank()) {
            return;
        }

        if (!isBlocked(baseCommand)) {
            return;
        }

        event.setResult(CommandExecuteEvent.CommandResult.denied());
        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(configManager.getBlockedMessage()));
    }

    @Subscribe(order = PostOrder.FIRST)
            final Player player = event.getPlayer();


        final Player player = event.getPlayer().get();

        if (player.hasPermission(BYPASS_PERMISSION) || player.hasPermission("ezcommandblocker.bypass.tab")) {
            return;
        }

        if (event.getPartialMessage().contains(" ")) {
            return;
        }

        event.getSuggestions().removeIf(suggestion -> isBlocked(VelocityConfigManager.normalizeCommand(suggestion)));
    }

    private boolean isBlocked(String baseCommand) {
        if (baseCommand.isBlank()) {
            return false;
        }

        final boolean containsColon = baseCommand.contains(":");
        final boolean listed = configManager.getCommandSet().contains(baseCommand);

        return (configManager.isBlockColonCommands() && containsColon)
                || (configManager.isUseCommandsAsWhitelist() ? !listed : listed);
    }

    private void reloadConfig() {
        try {
            configManager.load();
            logger.info("EzCommandBlocker Velocity config loaded successfully.");
        } catch (IOException exception) {
            logger.error("Failed to load EzCommandBlocker Velocity config.", exception);
        }
    }
}
