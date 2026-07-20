package dev.batterycraft.config;

import dev.batterycraft.profile.ManualMode;
import dev.batterycraft.profile.PowerProfile;
import dev.batterycraft.profile.ProfileSettings;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class BatteryCraftConfig {
    private final Path path;
    private final Map<PowerProfile, ProfileSettings> profiles = new EnumMap<>(PowerProfile.class);
    private boolean enabled = true;
    private boolean notifications = true;
    private boolean hudIndicator = true;
    private boolean sodiumIntegration = true;
    private boolean thermalMode;
    private int pollSeconds = 10;
    private int lowThreshold = 30;
    private int criticalThreshold = 15;
    private ManualMode manualMode = ManualMode.AUTOMATIC;

    private BatteryCraftConfig(Path path) {
        this.path = path;
        profiles.put(PowerProfile.BATTERY, new ProfileSettings(60, 10, 8, 1, true, true, 2, 0.80));
        profiles.put(PowerProfile.LOW_BATTERY, new ProfileSettings(45, 8, 6, 2, false, false, 1, 0.65));
        profiles.put(PowerProfile.CRITICAL_BATTERY, new ProfileSettings(30, 6, 5, 2, false, false, 0, 0.50));
    }

    public static BatteryCraftConfig load(Path path) {
        BatteryCraftConfig config = new BatteryCraftConfig(path);
        try {
            if (!Files.exists(path)) {
                config.save();
                return config;
            }
            String json = Files.readString(path, StandardCharsets.UTF_8);
            config.enabled = bool(json, "enabled", config.enabled);
            config.notifications = bool(json, "notifications", config.notifications);
            config.hudIndicator = bool(json, "hudIndicator", config.hudIndicator);
            config.sodiumIntegration = bool(json, "sodiumIntegration", config.sodiumIntegration);
            config.thermalMode = bool(json, "thermalMode", config.thermalMode);
            config.pollSeconds = bounded(integer(json, "pollSeconds", config.pollSeconds), 5, 300);
            config.lowThreshold = bounded(integer(json, "lowThreshold", config.lowThreshold), 2, 99);
            config.criticalThreshold = bounded(integer(json, "criticalThreshold", config.criticalThreshold), 1, config.lowThreshold - 1);
            config.manualMode = enumValue(json, "manualMode", ManualMode.class, config.manualMode);
            config.loadProfile(json, PowerProfile.BATTERY, "battery");
            config.loadProfile(json, PowerProfile.LOW_BATTERY, "lowBattery");
            config.loadProfile(json, PowerProfile.CRITICAL_BATTERY, "criticalBattery");
        } catch (IOException | RuntimeException error) {
            System.err.println("[BatteryCraft] Invalid config; defaults will be used: " + error.getMessage());
        }
        return config;
    }

    private void loadProfile(String json, PowerProfile profile, String prefix) {
        ProfileSettings old = profiles.get(profile);
        profiles.put(profile, new ProfileSettings(
                bounded(integer(json, prefix + ".maxFps", old.maxFps()), 15, 260),
                bounded(integer(json, prefix + ".renderDistance", old.renderDistance()), 2, 64),
                bounded(integer(json, prefix + ".simulationDistance", old.simulationDistance()), 2, 32),
                bounded(integer(json, prefix + ".particleLevel", old.particleLevel()), 0, 2),
                bool(json, prefix + ".clouds", old.clouds()),
                bool(json, prefix + ".entityShadows", old.entityShadows()),
                bounded(integer(json, prefix + ".biomeBlendRadius", old.biomeBlendRadius()), 0, 7),
                bounded(decimal(json, prefix + ".entityDistanceScale", old.entityDistanceScale()), 0.25, 1.0)
        ));
    }

    public synchronized void save() throws IOException {
        Files.createDirectories(path.getParent());
        StringBuilder json = new StringBuilder("{\n")
                .append(line("enabled", enabled)).append(line("notifications", notifications))
                .append(line("hudIndicator", hudIndicator)).append(line("sodiumIntegration", sodiumIntegration))
                .append(line("thermalMode", thermalMode)).append(line("pollSeconds", pollSeconds))
                .append(line("lowThreshold", lowThreshold)).append(line("criticalThreshold", criticalThreshold))
                .append(line("manualMode", manualMode.name()));
        appendProfile(json, "battery", profiles.get(PowerProfile.BATTERY));
        appendProfile(json, "lowBattery", profiles.get(PowerProfile.LOW_BATTERY));
        appendProfile(json, "criticalBattery", profiles.get(PowerProfile.CRITICAL_BATTERY));
        int comma = json.lastIndexOf(",");
        json.replace(comma, comma + 1, "");
        json.append("}\n");
        Files.writeString(path, json, StandardCharsets.UTF_8);
    }

    private static void appendProfile(StringBuilder json, String prefix, ProfileSettings profile) {
        json.append(line(prefix + ".maxFps", profile.maxFps()))
                .append(line(prefix + ".renderDistance", profile.renderDistance()))
                .append(line(prefix + ".simulationDistance", profile.simulationDistance()))
                .append(line(prefix + ".particleLevel", profile.particleLevel()))
                .append(line(prefix + ".clouds", profile.clouds()))
                .append(line(prefix + ".entityShadows", profile.entityShadows()))
                .append(line(prefix + ".biomeBlendRadius", profile.biomeBlendRadius()))
                .append(line(prefix + ".entityDistanceScale", profile.entityDistanceScale()));
    }

    private static String line(String key, Object value) {
        String encoded = value instanceof String ? "\"" + value + "\"" : String.valueOf(value);
        return "  \"" + key + "\": " + encoded + ",\n";
    }

    private static int integer(String json, String key, int fallback) {
        Matcher matcher = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*(-?\\d+)").matcher(json);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : fallback;
    }

    private static double decimal(String json, String key, double fallback) {
        Matcher matcher = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*([0-9.]+)").matcher(json);
        return matcher.find() ? Double.parseDouble(matcher.group(1)) : fallback;
    }

    private static boolean bool(String json, String key, boolean fallback) {
        Matcher matcher = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*(true|false)").matcher(json);
        return matcher.find() ? Boolean.parseBoolean(matcher.group(1)) : fallback;
    }

    private static <E extends Enum<E>> E enumValue(String json, String key, Class<E> type, E fallback) {
        Matcher matcher = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*\\\"([^\"]+)\\\"").matcher(json);
        if (!matcher.find()) return fallback;
        try { return Enum.valueOf(type, matcher.group(1)); } catch (IllegalArgumentException ignored) { return fallback; }
    }

    private static int bounded(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }
    private static double bounded(double value, double min, double max) { return Math.max(min, Math.min(max, value)); }

    public synchronized ProfileSettings profile(PowerProfile profile) { return profiles.get(profile); }
    public synchronized void profile(PowerProfile profile, ProfileSettings settings) { profiles.put(profile, settings); }
    public Path path() { return path; }
    public boolean enabled() { return enabled; }
    public void enabled(boolean value) { enabled = value; }
    public boolean notifications() { return notifications; }
    public void notifications(boolean value) { notifications = value; }
    public boolean hudIndicator() { return hudIndicator; }
    public void hudIndicator(boolean value) { hudIndicator = value; }
    public boolean sodiumIntegration() { return sodiumIntegration; }
    public void sodiumIntegration(boolean value) { sodiumIntegration = value; }
    public boolean thermalMode() { return thermalMode; }
    public void thermalMode(boolean value) { thermalMode = value; }
    public int pollSeconds() { return pollSeconds; }
    public void pollSeconds(int value) { pollSeconds = bounded(value, 5, 300); }
    public int lowThreshold() { return lowThreshold; }
    public void lowThreshold(int value) { lowThreshold = bounded(value, 2, 99); }
    public int criticalThreshold() { return criticalThreshold; }
    public void criticalThreshold(int value) { criticalThreshold = bounded(value, 1, lowThreshold - 1); }
    public ManualMode manualMode() { return manualMode; }
    public void manualMode(ManualMode value) { manualMode = value; }
}
