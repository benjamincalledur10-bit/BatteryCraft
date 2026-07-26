# BatteryCraft

BatteryCraft is a client-side Fabric mod that automatically reduces Minecraft's power usage when a MacBook runs on battery.

## 1.0.0-beta.3

- Automatic battery, low-battery and emergency profiles.
- Restores the player's original settings when power returns, including after an unexpected game shutdown.
- Shows the macOS battery-time estimate in the optional HUD indicator.
- Uses a configurable transition delay to prevent rapid profile switching.
- Two native Minecraft key mappings appear under Options > Controls > Key Binds > BatteryCraft.
- `B` cycles through automatic, manual and disabled modes by default.
- `O` opens the visual configuration screen by default.
- Both keys can be reassigned or unbound by the player and work normally on macOS.
- Optional notifications and in-game status indicator.
- Configurable HUD refresh interval.
- Editable FPS, render distance, simulation distance, particles, clouds, entity shadows, biome blend and entity distance.
- Sodium detection and optional entity-distance integration.
- Experimental macOS thermal-warning mode.
- Local session summary with battery and profile-change statistics.
- English/Spanish configuration UI and a local diagnostics panel.
- No administrator privileges, telemetry or Internet connection.

The configuration is stored at `config/batterycraft.json`. While a power-saving profile is active,
the original Minecraft values are protected in `config/batterycraft-recovery.properties`; the file is
removed automatically after a successful restoration. Existing configurations are upgraded with safe defaults.

## Downloads

- `batterycraft-1.0.0-beta.3-mc1.21.jar`: Minecraft 1.21.x
- `batterycraft-1.0.0-beta.3-mc26.1.jar`: Minecraft 26.1.x
- `batterycraft-1.0.0-beta.3-mc26.2.jar`: Minecraft 26.2.x

Fabric API is required. Minecraft 1.21.x needs Java 21; Minecraft 26.1.x and 26.2.x need Java 25.

## Tested combinations

- Minecraft 1.21.11 with Keo Optimized.
- Minecraft 26.1.2 with SodiumPlus.
- Minecraft 26.2 with Fabulously Optimized and Lumina shaders.

## Build

```bash
./gradlew clean test releaseJars
```

## License

MIT
