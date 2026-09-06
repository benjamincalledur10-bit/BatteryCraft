package dev.batterycraft;

import dev.batterycraft.battery.BatteryStatus;
import dev.batterycraft.battery.MacBatteryProvider;
import dev.batterycraft.battery.MacThermalProvider;
import dev.batterycraft.compat.MinecraftClientBridge;
import dev.batterycraft.compat.MinecraftSettingsAdapter;
import dev.batterycraft.compat.ConfigurableKeyMappings;
import dev.batterycraft.config.BatteryCraftConfig;
import dev.batterycraft.profile.ManualMode;
import dev.batterycraft.profile.PowerProfile;
import dev.batterycraft.profile.ProfileSettings;
import dev.batterycraft.profile.ProfileStabilizer;
import dev.batterycraft.stats.SessionStats;
import dev.batterycraft.ui.BatteryCraftConfigScreen;
import dev.batterycraft.ui.LocalizedText;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class BatteryCraftClient implements ClientModInitializer {
    private static BatteryCraftClient instance;
    public static final String VERSION = "1.0.0-beta.4";
    private final MacBatteryProvider batteryProvider = new MacBatteryProvider();
    private final MacThermalProvider thermalProvider = new MacThermalProvider();
    private final MinecraftSettingsAdapter settings = new MinecraftSettingsAdapter();
    private final MinecraftClientBridge client = new MinecraftClientBridge();
    private final ConfigurableKeyMappings keyMappings = new ConfigurableKeyMappings();
    private final SessionStats stats = new SessionStats();
    private final ProfileStabilizer stabilizer = new ProfileStabilizer();
    private final dev.batterycraft.ui.StatusMessageTimer statusTimer = new dev.batterycraft.ui.StatusMessageTimer();
    private volatile PowerProfile activeProfile;
    private volatile BatteryStatus lastBattery;
    private volatile long nextBatteryRead;
    private volatile long nextThermalRead;
    private volatile boolean thermalWarning;
    private BatteryCraftConfig config;
    private boolean sodiumLoaded;

    @Override
    public void onInitializeClient() {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve("batterycraft.json");
        config = BatteryCraftConfig.load(configPath);
        settings.configureRecovery(configPath.resolveSibling("batterycraft-recovery.properties"));
        sodiumLoaded = FabricLoader.getInstance().isModLoaded("sodium");
        instance = this;
        boolean sodiumMenu = hasSodiumConfigApi();
        boolean mappingsRegistered = !sodiumMenu && keyMappings.register();
        System.out.printf("[BatteryCraft] Starting %s macOS=%s arch=%s sodium=%s keyMappings=%s recovery=%s%n",
                VERSION, batteryProvider.isSupported(), System.getProperty("os.arch", "unknown"),
                sodiumLoaded, mappingsRegistered, settings.hasPendingRecovery());
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
        if (!sodiumMenu) service.scheduleWithFixedDelay(this::hotkeys, 2, 150, TimeUnit.MILLISECONDS);
    }

    private static boolean hasSodiumConfigApi() {
        try {
            Class.forName("net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint", false, BatteryCraftClient.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException ignored) { return false; }
    }

    public static void registerSodiumOptions(Object builder) {
        if (instance == null) throw new IllegalStateException("BatteryCraft client has not initialized");
        new dev.batterycraft.compat.sodium.SodiumOptions(instance.config, () -> {
            try { instance.config.save(); }
            catch (java.io.IOException error) { throw new IllegalStateException("Could not save BatteryCraft options", error); }
            instance.configurationChanged();
        }).register(builder);
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
        if (statusTimer.due(System.nanoTime(), config.enabled() && config.manualMode() != ManualMode.DISABLED
                && config.hudIndicator() && lastBattery != null, config.hudIntervalSeconds())) {
            String remaining = lastBattery.remainingText().isEmpty() ? "" : " | " + lastBattery.remainingText();
            client.message("BatteryCraft: " + LocalizedText.profile(activeProfile) + " | "
                    + lastBattery.percentage() + "%" + remaining, true);
        }
    }

    private synchronized void applySelectedProfile() {
        if (lastBattery == null) return;
        PowerProfile selected = stabilizer.select(selectProfile(), System.currentTimeMillis(), config.transitionDelaySeconds());
        if (selected == activeProfile) return;
        ProfileSettings values = selected == PowerProfile.PLUGGED_IN ? null : config.profile(selected);
        boolean sodiumIntegration = sodiumLoaded && config.sodiumIntegration();
        if (settings.apply(selected, values, sodiumIntegration)) {
            activeProfile = selected;
            stats.profileChanged(selected);
            String message = selected == PowerProfile.PLUGGED_IN
                    ? "BatteryCraft: " + LocalizedText.value("original settings restored", "ajustes originales restaurados")
                    : "BatteryCraft: " + LocalizedText.profile(selected) + " - " + values.maxFps() + " FPS";
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
        while (keyMappings.consumeCycle()) {
            config.manualMode(config.manualMode().next());
            saveConfig();
            activeProfile = null;
            stabilizer.reset();
            applySelectedProfile();
            client.message("BatteryCraft " + LocalizedText.value("mode", "modo") + ": " + config.manualMode(), false);
        }
        while (keyMappings.consumeConfig()) {
            BatteryCraftConfigScreen.open(config, this::configurationChanged, stats::summary, this::diagnostics);
        }
    }

    private void configurationChanged() {
        statusTimer.reset();
        activeProfile = null;
        stabilizer.reset();
        nextBatteryRead = 0;
        applySelectedProfile();
    }

    private void saveConfig() {
        try { config.save(); }
        catch (Exception error) { System.err.println("[BatteryCraft] Could not save config: " + error.getMessage()); }
    }

    private String diagnostics() {
        String battery = lastBattery == null ? LocalizedText.value("waiting", "esperando")
                : lastBattery.percentage() + "% " + lastBattery.remainingText();
        return "<html><b>" + LocalizedText.value("Diagnostics", "Diagnóstico") + ":</b> macOS="
                + batteryProvider.isSupported() + " | arch=" + System.getProperty("os.arch", "unknown")
                + " | Sodium=" + sodiumLoaded + " | thermal=" + thermalWarning
                + " | recovery=" + settings.hasPendingRecovery() + "<br>"
                + LocalizedText.value("Battery", "Batería") + "=" + battery + "</html>";
    }
}
