package dev.batterycraft.battery;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MacBatteryProvider {
    private static final Pattern PERCENTAGE = Pattern.compile("(\\d{1,3})%");

    public boolean isSupported() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("mac");
    }

    public Optional<BatteryStatus> read() {
        if (!isSupported()) return Optional.empty();
        Process process = null;
        try {
            process = new ProcessBuilder("/usr/bin/pmset", "-g", "batt").redirectErrorStream(true).start();
            if (!process.waitFor(3, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return Optional.empty();
            }
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) output.append(line).append('\n');
            }
            Matcher matcher = PERCENTAGE.matcher(output);
            if (!matcher.find()) return Optional.empty();
            String normalized = output.toString().toLowerCase(Locale.ROOT);
            int percentage = Integer.parseInt(matcher.group(1));
            boolean pluggedIn = normalized.contains("ac power");
            boolean charging = normalized.contains("charging") && !normalized.contains("not charging");
            return Optional.of(new BatteryStatus(percentage, pluggedIn, charging));
        } catch (IOException | InterruptedException | NumberFormatException error) {
            if (error instanceof InterruptedException) Thread.currentThread().interrupt();
            return Optional.empty();
        } finally {
            if (process != null && process.isAlive()) process.destroyForcibly();
        }
    }
}
