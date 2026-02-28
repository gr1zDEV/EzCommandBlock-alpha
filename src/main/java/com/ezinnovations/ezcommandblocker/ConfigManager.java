package com.ezinnovations.ezcommandblocker;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class ConfigManager {
    private final JavaPlugin plugin;

    private volatile Map<String, TabGroup> tabGroups = Map.of();
    private volatile Set<String> commandSet = Set.of();
    private volatile boolean blockColonCommands;
    private volatile boolean useCommandsAsWhitelist;
    private volatile boolean updateNotify;
    private volatile boolean networkMode;
    private volatile boolean legacySupport;
    private volatile List<String> defaultActions = List.of();
    private volatile Map<String, CustomActionGroup> customActionGroups = Map.of();

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        cache(plugin.getConfig());
    }

    public void reload() {
        plugin.reloadConfig();
        cache(plugin.getConfig());
    }

    public Map<String, TabGroup> getTabGroups() {
        return tabGroups;
    }

    public Set<String> getCommandSet() {
        return commandSet;
    }

    public boolean isBlockColonCommands() {
        return blockColonCommands;
    }

    public boolean isUseCommandsAsWhitelist() {
        return useCommandsAsWhitelist;
    }

    public boolean isUpdateNotify() {
        return updateNotify;
    }

    public boolean isNetworkMode() {
        return networkMode;
    }

    public boolean isLegacySupport() {
        return legacySupport;
    }

    public List<String> getDefaultActions() {
        return defaultActions;
    }

    public Map<String, CustomActionGroup> getCustomActionGroups() {
        return customActionGroups;
    }

    public TabGroup resolveActiveGroup(org.bukkit.entity.Player player) {
        final Map<String, TabGroup> groups = tabGroups;
        final TabGroup fallback = groups.getOrDefault("default", new TabGroup(0, null, List.of()));

        return groups.entrySet().stream()
                .filter(entry -> player.hasPermission("ezcommandblocker.tab." + entry.getKey()))
                .map(Map.Entry::getValue)
                .max(Comparator.comparingInt(TabGroup::priority))
                .orElse(fallback);
    }

    public Set<String> resolveGroupCommands(String groupName) {
        final Map<String, TabGroup> groups = tabGroups;
        if (!groups.containsKey(groupName)) {
            return Set.of();
        }

        final Set<String> resolved = new LinkedHashSet<>();
        final Set<String> visiting = new HashSet<>();
        final Deque<String> stack = new ArrayDeque<>();
        stack.push(groupName);

        while (!stack.isEmpty()) {
            final String currentName = stack.pop();
            if (!visiting.add(currentName)) {
                continue;
            }
            final TabGroup current = groups.get(currentName);
            if (current == null) {
                continue;
            }
            resolved.addAll(current.commands());
            if (current.parent() != null && !current.parent().isBlank()) {
                stack.push(current.parent().toLowerCase(Locale.ROOT));
            }
        }

        return Collections.unmodifiableSet(resolved);
    }

    private void cache(FileConfiguration config) {
        blockColonCommands = config.getBoolean("block_colon_commands", false);
        useCommandsAsWhitelist = config.getBoolean("use_commands_as_whitelist", true);
        updateNotify = config.getBoolean("update_notify", true);
        networkMode = config.getBoolean("is_network", false);
        legacySupport = config.getBoolean("legacy_support", false);

        commandSet = Collections.unmodifiableSet(normalizeCommands(config.getStringList("commands")));
        defaultActions = List.copyOf(config.getStringList("blocked_command_default_actions"));
        customActionGroups = Collections.unmodifiableMap(loadCustomActionGroups(config.getConfigurationSection("custom_commands_actions")));
        tabGroups = Collections.unmodifiableMap(loadTabGroups(config.getConfigurationSection("tab")));
    }

    private Map<String, TabGroup> loadTabGroups(ConfigurationSection section) {
        if (section == null) {
            return Map.of("default", new TabGroup(0, null, List.of()));
        }

        final Map<String, TabGroup> groups = new HashMap<>();
        for (String key : section.getKeys(false)) {
            final ConfigurationSection groupSection = section.getConfigurationSection(key);
            if (groupSection == null) {
                continue;
            }
            final int priority = groupSection.getInt("priority", 0);
            final String parent = normalizeGroup(groupSection.getString("extends"));
            final List<String> commands = List.copyOf(normalizeCommands(groupSection.getStringList("commands")));
            groups.put(key.toLowerCase(Locale.ROOT), new TabGroup(priority, parent, commands));
        }

        groups.putIfAbsent("default", new TabGroup(0, null, List.of()));
        return groups;
    }

    private Map<String, CustomActionGroup> loadCustomActionGroups(ConfigurationSection section) {
        if (section == null) {
            return Map.of();
        }

        final Map<String, CustomActionGroup> groups = new HashMap<>();
        for (String key : section.getKeys(false)) {
            final ConfigurationSection groupSection = section.getConfigurationSection(key);
            if (groupSection == null) {
                continue;
            }
            final Set<String> commands = Collections.unmodifiableSet(normalizeCommands(groupSection.getStringList("commands")));
            final List<String> actions = List.copyOf(groupSection.getStringList("actions"));
            groups.put(key, new CustomActionGroup(commands, actions));
        }
        return groups;
    }

    private Set<String> normalizeCommands(Collection<String> rawCommands) {
        final Set<String> normalized = new LinkedHashSet<>();
        for (String command : rawCommands) {
            final String value = normalizeCommand(command);
            if (!value.isBlank()) {
                normalized.add(value);
            }
        }
        return normalized;
    }

    public static String normalizeCommand(String command) {
        if (command == null) {
            return "";
        }

        final String trimmed = command.trim().toLowerCase(Locale.ROOT);
        if (trimmed.isEmpty()) {
            return "";
        }

        final String noSlash = trimmed.startsWith("/") ? trimmed.substring(1) : trimmed;
        return noSlash.split("\\s+")[0];
    }

    private String normalizeGroup(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        return input.toLowerCase(Locale.ROOT);
    }

    public record CustomActionGroup(Set<String> commands, List<String> actions) {
    }
}
