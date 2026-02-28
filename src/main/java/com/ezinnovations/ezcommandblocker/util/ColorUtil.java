package com.ezinnovations.ezcommandblocker.util;

import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ColorUtil {
    private static final Pattern HEX_PATTERN = Pattern.compile("&?#([A-Fa-f0-9]{6})");

    private ColorUtil() {
    }

    public static String colorize(String input) {
        if (input == null) {
            return "";
        }

        final Matcher matcher = HEX_PATTERN.matcher(input);
        final StringBuilder builder = new StringBuilder();

        while (matcher.find()) {
            final String hex = matcher.group(1);
            final String replacement = "§x§" + hex.charAt(0)
                    + "§" + hex.charAt(1)
                    + "§" + hex.charAt(2)
                    + "§" + hex.charAt(3)
                    + "§" + hex.charAt(4)
                    + "§" + hex.charAt(5);
            matcher.appendReplacement(builder, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(builder);
        return ChatColor.translateAlternateColorCodes('&', builder.toString());
    }
}
