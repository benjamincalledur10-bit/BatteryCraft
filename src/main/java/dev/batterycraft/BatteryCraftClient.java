package dev.batterycraft;

import dev.batterycraft.battery.BatteryStatus;
import dev.batterycraft.battery.MacBatteryProvider;
import dev.batterycraft.battery.MacThermalProvider;
import dev.batterycraft.compat.MinecraftClientBridge;
import dev.batterycraft.compat.MinecraftSettingsAdapter;
import dev.batterycraft.config.BatteryCraftConfig;
import dev.batterycraft.profile.ManualMode;
import dev.batterycraft.profile.PowerProfile;
import dev.batterycraft.profile.ProfileSettings;
import dev.batterycraft.stats.SessionStats;
import dev.batterycraft.ui.BatteryCraftConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class BatteryCraftClient implements ClientModInitializer {
    public static final String VERSION = "1.0.0-beta.1";
    private final MacBatteryProvider batteryProvider = new MacBatteryProvider();
    private final MacThermalProvider thermalProvider = new MacThermalProvider();
    private final MinecraftSettingsAdapter settings = new MinecraftSettingsAdapter();
    private final MinecraftClientBridge client = new MinecraftClientBridge();
    private final SessionStats stats = new SessionStats();
    private volatile PowerProfile activeProfile;
    private volatile BatteryStatus lastBattery;
    private volatile long nextBatteryRead;
    private volatile long nextThermalRead;
    private volatile boolean thermalWarning;
    private volatile boolean cyclePressed;
    private volatile boolean configPressed;
    private BatteryCraftConfig config;
    private boolean sodiumLoaded;

    @Override
    public void onInitializeClient() {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve("batterycraft.json");
        config = BatteryCraftConfig.load(configPath);
        sodiumLoaded = FabricLoader.getInstance().isModLoaded("sodium");
        System.out.printf("[BatteryCraft] Starting %s macOS=%s sodium=%s%n", VERSION, batteryProvider.isSupported(), sodiumLoaded);
        if (!batteryProvider.isSupported()) {
            System.out.println("[BatteryCraft] macOS was not detected; automatic profiles are unavailable.");
            return;
        }

        ScheduledExecutorService service = Executors.newScheduledThreadPool(2, runnable -> {
            Thread thread = new Thread(runnable, "BatteryCraft monitor");
            thread.setDaemon(true);
            return thread;
        });
        service.scheduleWithFixedDelay(this::monitor, 2, 1, TimeUnit.SECONDS);
        service.scheduleWithFixedDelay(this::hotkeys, 2, 150, TimeUnit.MILLISECONDS);
    }

    private void monitor() {
        long now = System.currentTimeMillis();
        if (now >= nextBatteryRead) {
            nextBatteryRead = now + config.pollSeconds() * 1_000L;
            Optional<BatteryStatus> reading = batteryProvider.read();
            if (reading.isPresent()) {
                lastBattery = reading.get();
                stats.battery(lastBattery);
                applySelectedProfile();
            }
        }
        if (config.thermalMode() && now >= nextThermalRead) {
            nextThermalRead = now + 60_000L;
            thermalWarning = thermalProvider.hasThermalWarning();
            if (thermalWarning) applySelectedProfile();
        }
        if (config.hudIndicator() && lastBattery != null && now / 5_000 != (now - 1_000) / 5_000) {
            client.message("BatteryCraft: " + displayName(activeProfile) + " | " + lastBattery.percentage() + "%", true);
        }
    }

    private synchronized void applySelectedProfile() {
        if (lastBattery == null) return;
        PowerProfile selected = selectProfile();
        if (selected == activeProfile) return;
        ProfileSettings values = selected == PowerProfile.PLUGGED_IN ? null : config.profile(selected);
        boolean sodiumIntegration = sodiumLoaded && config.sodiumIntegration();
        if (settings.apply(selected, values, sodiumIntegration)) {
            activeProfile = selected;
            stats.profileChanged(selected);
            String message = selected == PowerProfile.PLUGGED_IN
                    ? "BatteryCraft: ajustes originales restaurados"
                    : "BatteryCraft: " + displayName(selected) + " - " + values.maxFps() + " FPS";
            if (config.notifications()) client.message(message, false);
            System.out.printf("[BatteryCraft] Profile=%s battery=%d%% pluggedIn=%s thermal=%s sodium=%s%n",
                    selected, lastBattery.percentage(), lastBattery.pluggedIn(), thermalWarning, sodiumIntegration);
        }
    }

    private PowerProfile selectProfile() {
        if (!config.enabled() || config.manualMode() == ManualMode.DISABLED) return PowerProfile.PLUGGED_IN;
        if (config.manualMode() != ManualMode.AUTOMATIC) return config.manualMode().forcedProfile();
        PowerProfile automatic = PowerProfile.select(lastBattery, config.lowThreshold(), config.criticalThreshold());
        if (thermalWarning && automatic == PowerProfile.BATTERY) return PowerProfile.LOW_BATTERY;
        return automatic;
    }

    private void hotkeys() {
        boolean cycleDown = client.keyDown(config.cycleKey());
        if (cycleDown && !cyclePressed) {
            config.manualMode(config.manualMode().next());
            saveConfig();
            activeProfile = null;
            applySelectedProfile();
            client.message("BatteryCraft modo: " + config.manualMode(), false);
        }
        cyclePressed = cycleDown;

        boolean configDown = client.keyDown(config.configKey());
        if (configDown && !configPressed) {
            BatteryCraftConfigScreen.open(config, this::configurationChanged, stats::summary);
        }
        configPressed = configDown;
    }

    private void configurationChanged() {
        activeProfile = null;
        nextBatteryRead = 0;
        applySelectedProfile();
    }

    private void saveConfig() {
        try { config.save(); }
        catch (Exception error) { System.err.println("[BatteryCraft] Could not save config: " + error.getMessage()); }
    }

    private static String displayName(PowerProfile profile) {
        if (profile == null) return "Iniciando";
        return switch (profile) {
            case PLUGGED_IN -> "Conectado";
            case BATTERY -> "Ahorro";
            case LOW_BATTERY -> "Ahorro intenso";
            case CRITICAL_BATTERY -> "Emergencia";
        };
    }
}
