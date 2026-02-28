package com.ezinnovations.ezcommandblocker;

import com.ezinnovations.ezcommandblocker.util.ColorUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class EzCommandBlocker extends JavaPlugin implements CommandExecutor {
    public static final String PREFIX = "&8[&b&lEz&8]";

    private ConfigManager configManager;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.configManager.load();

        final ActionExecutor actionExecutor = new ActionExecutor(this);
        getServer().getPluginManager().registerEvents(new CommandBlockerListener(configManager, actionExecutor), this);
        getServer().getPluginManager().registerEvents(new TabCompleteListener(configManager), this);

        if (getCommand("ezcommandblocker") != null) {
            getCommand("ezcommandblocker").setExecutor(this);
        }

        getLogger().info("EzCommandBlocker enabled. Folia mode: " + com.ezinnovations.ezcommandblocker.util.FoliaUtil.isFolia());
    }

    @Override
    public void onDisable() {
        getLogger().info("EzCommandBlocker disabled.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("ezcommandblocker.admin")) {
                sender.sendMessage(ColorUtil.colorize(PREFIX + " &cYou do not have permission to do that."));
                return true;
            }

            configManager.reload();
            sender.sendMessage(ColorUtil.colorize(PREFIX + " &aConfiguration reloaded successfully."));
            return true;
        }

        sender.sendMessage(ColorUtil.colorize(PREFIX + " &7Usage: &b/" + label + " reload"));
        return true;
    }
}
