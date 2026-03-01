package com.ezinnovations.ezcommandblocker.velocity;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class VelocityConfigManager {
    private final Path dataDirectory;

    private volatile Set<String> commandSet = Set.of();
    private volatile boolean blockColonCommands;
    private volatile boolean useCommandsAsWhitelist;
    private volatile String blockedMessage = "&cYou are not allowed to use this command.";

    public VelocityConfigManager(Path dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    public void load() throws IOException {
        Files.createDirectories(dataDirectory);

        final Path configPath = dataDirectory.resolve("config.yml");
        if (Files.notExists(configPath)) {
            try (InputStream input = getClass().getClassLoader().getResourceAsStream("velocity-config.yml")) {
                if (input == null) {
                    throw new IOException("Missing bundled velocity-config.yml resource");
                }
                try (Writer writer = Files.newBufferedWriter(configPath)) {
                    writer.write(new String(input.readAllBytes(), StandardCharsets.UTF_8));
                }
            }
        }

        final LoaderOptions options = new LoaderOptions();
        options.setAllowDuplicateKeys(false);
        final Yaml yaml = new Yaml(new SafeConstructor(options));

        try (InputStream input = Files.newInputStream(configPath)) {
            final Object loaded = yaml.load(input);
            if (!(loaded instanceof Map<?, ?> map)) {
                applyDefaults();
                return;
            }

            blockColonCommands = readBoolean(map, "block_colon_commands", false);
            useCommandsAsWhitelist = readBoolean(map, "use_commands_as_whitelist", true);
            blockedMessage = Objects.toString(map.getOrDefault("blocked_message", blockedMessage), blockedMessage);
            commandSet = Collections.unmodifiableSet(normalizeCommands(map.get("commands")));
        }
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

    public String getBlockedMessage() {
        return blockedMessage;
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

    private Set<String> normalizeCommands(Object rawCommands) {
        if (!(rawCommands instanceof Collection<?> commands)) {
            return Set.of();
        }

        final Set<String> normalized = new LinkedHashSet<>();
        for (Object raw : commands) {
            final String value = normalizeCommand(Objects.toString(raw, ""));
            if (!value.isBlank()) {
                normalized.add(value);
            }
        }
        return normalized;
    }

    private boolean readBoolean(Map<?, ?> map, String key, boolean fallback) {
        final Object value = map.get(key);
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof String text) {
            return Boolean.parseBoolean(text);
        }
        return fallback;
    }

    private void applyDefaults() {
        blockColonCommands = false;
        useCommandsAsWhitelist = true;
        blockedMessage = "&cYou are not allowed to use this command.";
        commandSet = Set.of();
    }
}
