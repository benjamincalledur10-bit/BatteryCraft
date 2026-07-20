package dev.batterycraft;

import dev.batterycraft.battery.BatteryStatus;
import dev.batterycraft.battery.MacBatteryProvider;
import dev.batterycraft.compat.MinecraftSettingsAdapter;
import dev.batterycraft.config.BatteryCraftConfig;
import dev.batterycraft.profile.PowerProfile;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class BatteryCraftClient implements ClientModInitializer {
    public static final String VERSION = "0.0.1-alpha";
    private final MacBatteryProvider batteryProvider = new MacBatteryProvider();
    private final MinecraftSettingsAdapter settings = new MinecraftSettingsAdapter();
    private volatile PowerProfile activeProfile;

    @Override
    public void onInitializeClient() {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve("batterycraft.json");
        BatteryCraftConfig config = BatteryCraftConfig.load(configPath);
        System.out.println("[BatteryCraft] Starting " + VERSION + " on macOS=" + batteryProvider.isSupported());
        if (!config.enabled() || !batteryProvider.isSupported()) {
            System.out.println("[BatteryCraft] Automatic profiles are disabled or this is not macOS.");
            return;
        }
        ScheduledExecutorService monitor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "BatteryCraft battery monitor");
            thread.setDaemon(true);
            return thread;
        });
        monitor.scheduleWithFixedDelay(() -> update(config), 2, config.pollSeconds(), TimeUnit.SECONDS);
    }

    private void update(BatteryCraftConfig config) {
        Optional<BatteryStatus> reading = batteryProvider.read();
        if (reading.isEmpty()) {
            System.err.println("[BatteryCraft] Battery status is temporarily unavailable; no settings were changed.");
            return;
        }
        BatteryStatus status = reading.get();
        PowerProfile selected = PowerProfile.select(status, config.lowThreshold(), config.criticalThreshold());
        if (selected == activeProfile) return;
        if (settings.apply(selected)) {
            activeProfile = selected;
            System.out.printf("[BatteryCraft] Profile=%s battery=%d%% pluggedIn=%s%n", selected, status.percentage(), status.pluggedIn());
        }
    }
}
