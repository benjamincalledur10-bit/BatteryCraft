# BatteryCraft

BatteryCraft is a client-side Fabric mod that automatically reduces Minecraft's power usage when a MacBook runs on battery.

## 1.0.0-beta.1

- Automatic battery, low-battery and emergency profiles.
- Restores the player's original settings when power returns.
- F9 opens the visual configuration screen.
- F8 cycles through automatic, manual and disabled modes.
- Optional notifications and in-game status indicator.
- Editable FPS, render distance, simulation distance, particles, clouds, entity shadows, biome blend and entity distance.
- Sodium detection and optional entity-distance integration.
- Experimental macOS thermal-warning mode.
- Local session summary with battery and profile-change statistics.
- No administrator privileges, telemetry or Internet connection.

The configuration is stored at `config/batterycraft.json`. Existing alpha configuration files are upgraded with safe defaults.

## Downloads

- `batterycraft-1.0.0-beta.1-mc1.21.jar`: Minecraft 1.21.x
- `batterycraft-1.0.0-beta.1-mc26.1.jar`: Minecraft 26.1.x
- `batterycraft-1.0.0-beta.1-mc26.2.jar`: Minecraft 26.2.x

Fabric API is not required. Minecraft 1.21.x needs Java 21; Minecraft 26.1.x and 26.2.x need Java 25.

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
