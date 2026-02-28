package com.ezinnovations.ezcommandblocker.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class FoliaUtil {
    private static final boolean FOLIA = hasFoliaClass();

    private FoliaUtil() {
    }

    public static boolean isFolia() {
        return FOLIA;
    }

    public static void runTask(Plugin plugin, Player player, Runnable runnable) {
        if (isFolia()) {
            player.getScheduler().run(plugin, scheduledTask -> runnable.run(), null);
            return;
        }
        Bukkit.getScheduler().runTask(plugin, runnable);
    }

    public static void runTaskLater(Plugin plugin, Player player, Runnable runnable, long delayTicks) {
        if (isFolia()) {
            player.getScheduler().runDelayed(plugin, scheduledTask -> runnable.run(), null, delayTicks);
            return;
        }
        Bukkit.getScheduler().runTaskLater(plugin, runnable, delayTicks);
    }

    private static boolean hasFoliaClass() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }
}
