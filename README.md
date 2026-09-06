# BatteryCraft

**Play longer on your MacBook.** BatteryCraft is a client-side Fabric mod that automatically lowers Minecraft's power usage when you unplug your charger, then safely restores your original settings when external power returns.

## 1.0.0-beta.4

- Periodic center-screen status messages are now disabled by default, including after upgrading from beta.3.
- The optional status interval can now be configured in seconds or minutes (including 15 and 30 minutes).
- Added versioned configuration migrations for safer upgrades.
- New battery profiles prioritize lighter rendering: 120 FPS / 8 render chunks / 5 simulation chunks; low battery: 90 / 6 / 4; critical: 60 / 4 / 3.
- FPS values are configurable caps, not guaranteed frame rates. Set a profile cap to `0` to retain Minecraft's original FPS setting.
- Existing custom profiles are preserved; unchanged legacy defaults migrate to the new profiles.
- Visual profiles never increase your original render/simulation distances, entity distance, biome blend, clouds, shadows or particle density.
- Connecting the charger restores your original settings, including your FPS cap. BatteryCraft does not impose a minimum plugged-in frame rate.

Actual FPS and battery life depend on hardware, shaders, world complexity and other mods. Higher FPS can increase power use; beta.4 performance and autonomy gains have not yet been benchmarked in game.

- Automatic battery, low-battery and emergency profiles.
- Restores the player's original settings when power returns, including after an unexpected game shutdown.
- Shows the macOS battery-time estimate in the optional HUD indicator.
- Uses a configurable transition delay to prevent rapid profile switching.
- Two native Minecraft key mappings appear under Options > Controls > Key Binds > BatteryCraft.
- `B` cycles through automatic, manual and disabled modes by default.
- `O` opens the visual configuration screen by default.
- Both keys can be reassigned or unbound by the player and work normally on macOS.
- Optional notifications and periodic in-game status message.
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

## Why BatteryCraft?

- Works automatically in the background with battery, low-battery and critical profiles.
- Adjusts FPS, render distance, simulation distance, particles and other demanding settings.
- Smart Recovery protects your original settings—even if Minecraft closes unexpectedly.
- Includes editable profiles, an optional HUD, notifications and experimental thermal protection.
- Supports Sodium entity-distance integration when Sodium is installed.
- Runs entirely on your device: no telemetry, Internet connection or administrator access.

## Download

- [Modrinth](https://modrinth.com/mod/batterycraft)
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/batterycraft)
- [GitHub](https://github.com/benjamincalledur10-bit/BatteryCraft)

### Compatibility

- `batterycraft-1.0.0-beta.4-mc1.21.jar`: Minecraft 1.21.x
- `batterycraft-1.0.0-beta.4-mc26.1.jar`: Minecraft 26.1.x
- `batterycraft-1.0.0-beta.4-mc26.2.jar`: Minecraft 26.2.x

Fabric API is required. Minecraft 1.21.x needs Java 21; Minecraft 26.1.x and 26.2.x need Java 25.

BatteryCraft is designed for macOS laptops and is not required on the server.

## Installation

1. Install Fabric Loader and Fabric API for your Minecraft version.
2. Download the matching BatteryCraft file.
3. Place the file in your Minecraft `mods` folder.
4. Press `O` in game to configure BatteryCraft. Press `B` to cycle modes.

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
