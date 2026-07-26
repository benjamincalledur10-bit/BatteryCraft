package dev.batterycraft.compat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

final class RecoveryStore {
    private final Path path;

    RecoveryStore(Path path) {
        this.path = path;
    }

    synchronized Map<String, String> load() {
        Map<String, String> values = new HashMap<>();
        if (path == null || !Files.exists(path)) return values;
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(path)) {
            properties.load(input);
            for (String key : properties.stringPropertyNames()) values.put(key, properties.getProperty(key));
        } catch (IOException error) {
            System.err.println("[BatteryCraft] Could not read recovery snapshot: " + error.getMessage());
        }
        return values;
    }

    synchronized void save(Map<String, String> values) {
        if (path == null || values.isEmpty()) return;
        try {
            Files.createDirectories(path.getParent());
            Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
            Properties properties = new Properties();
            properties.putAll(values);
            try (OutputStream output = Files.newOutputStream(temporary)) {
                properties.store(output, "BatteryCraft original Minecraft settings");
            }
            try {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (java.nio.file.AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException error) {
            System.err.println("[BatteryCraft] Could not save recovery snapshot: " + error.getMessage());
        }
    }

    synchronized void clear() {
        if (path == null) return;
        try {
            Files.deleteIfExists(path);
        } catch (IOException error) {
            System.err.println("[BatteryCraft] Could not remove recovery snapshot: " + error.getMessage());
        }
    }

    boolean exists() {
        return path != null && Files.exists(path);
    }
}
