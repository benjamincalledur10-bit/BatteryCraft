package dev.batterycraft.battery;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public final class MacThermalProvider {
    public boolean hasThermalWarning() {
        Process process = null;
        try {
            process = new ProcessBuilder("/usr/bin/pmset", "-g", "therm").redirectErrorStream(true).start();
            if (!process.waitFor(3, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return false;
            }
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).toLowerCase(Locale.ROOT);
            return output.contains("warning") && !output.lines().allMatch(line -> line.isBlank() || line.contains("no "));
        } catch (IOException | InterruptedException error) {
            if (error instanceof InterruptedException) Thread.currentThread().interrupt();
            return false;
        } finally {
            if (process != null && process.isAlive()) process.destroyForcibly();
        }
    }
}
