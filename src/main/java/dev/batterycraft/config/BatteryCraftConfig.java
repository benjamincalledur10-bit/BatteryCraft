package dev.batterycraft.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record BatteryCraftConfig(boolean enabled, int pollSeconds, int lowThreshold, int criticalThreshold) {
    public static BatteryCraftConfig defaults() { return new BatteryCraftConfig(true, 10, 30, 15); }

    public static BatteryCraftConfig load(Path path) {
        BatteryCraftConfig defaults = defaults();
        try {
            if (!Files.exists(path)) {
                defaults.save(path);
                return defaults;
            }
            String json = Files.readString(path, StandardCharsets.UTF_8);
            int low = bounded(intValue(json, "lowThreshold", defaults.lowThreshold), 2, 99);
            int critical = bounded(intValue(json, "criticalThreshold", defaults.criticalThreshold), 1, low - 1);
            return new BatteryCraftConfig(
                    booleanValue(json, "enabled", defaults.enabled),
                    bounded(intValue(json, "pollSeconds", defaults.pollSeconds), 5, 300),
                    low,
                    critical
            );
        } catch (IOException | NumberFormatException error) {
            return defaults;
        }
    }

    public void save(Path path) throws IOException {
        Files.createDirectories(path.getParent());
        String json = "{\n" +
                "  \"enabled\": " + enabled + ",\n" +
                "  \"pollSeconds\": " + pollSeconds + ",\n" +
                "  \"lowThreshold\": " + lowThreshold + ",\n" +
                "  \"criticalThreshold\": " + criticalThreshold + "\n" +
                "}\n";
        Files.writeString(path, json, StandardCharsets.UTF_8);
    }

    private static int intValue(String json, String key, int fallback) {
        Matcher matcher = Pattern.compile("\\\"" + key + "\\\"\\s*:\\s*(\\d+)").matcher(json);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : fallback;
    }

    private static boolean booleanValue(String json, String key, boolean fallback) {
        Matcher matcher = Pattern.compile("\\\"" + key + "\\\"\\s*:\\s*(true|false)").matcher(json);
        return matcher.find() ? Boolean.parseBoolean(matcher.group(1)) : fallback;
    }

    private static int bounded(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
