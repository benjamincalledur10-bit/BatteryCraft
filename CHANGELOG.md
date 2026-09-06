# Changelog

## 1.0.0-beta.4 - 2026-09-06

### Clearer configuration

- Renamed the FPS control to "Maximum FPS on battery" and clarified that zero retains Minecraft's original cap.
- Added explicit 15 minutes + 0 seconds and 30 minutes + 0 seconds reminder examples.
- Updated the README with the beta.4 release and maintainer-confirmed 1.21.11/Sodium menu behavior, without claiming measured performance or autonomy gains.

### Startup fix after alpha.2

- Fixed a startup crash while registering Sodium options: groups and pages are now attached only after their contents are populated.
- Added a regression test using Sodium 0.8.12's actual builders, reproducing the original empty-group rejection and validating all five pages and 36 options.

### Native Sodium configuration

- Added BatteryCraft to Sodium's video-settings sidebar through its public Config API.
- Added native General, Notifications, Battery, Low battery and Critical battery pages with English/Spanish labels and tooltips.
- All 36 editable options use Sodium's Apply/cancel flow; interval minutes and seconds are combined when applying.
- Native-menu users no longer depend on or register the old B/O key bindings.
- Optional integration keeps Sodium out of the required runtime dependencies.
- Added contract tests against the published Sodium 0.8.12 API for registration, deferred saving, compound intervals and profile updates.

### Less intrusive status display

- Disabled the periodic center-screen status message by default.
- Existing beta.3 configurations are migrated once with the periodic message disabled.
- Renamed the option to make its behavior clear; players can still enable it manually.
- Increased the optional status interval limit to custom seconds or minutes (including 15 and 30 minutes).

### Rendering and frame rate

- New default caps of 120 / 90 / 60 FPS with lower rendering and simulation distances.
- Set a profile FPS cap to zero to retain Minecraft's original cap, including after switching from a capped profile.
- Preserve lighter original visual settings instead of increasing their workload.
- Migrate unchanged legacy profiles while preserving custom profiles.
- Periodic messages now wait a full interval after enabling or reconfiguring them and stop when the mod is disabled.
- These changes adjust rendering workload; FPS and battery-life gains require in-game benchmarking.

### Reliability

- Added a configuration version for safe future migrations.
- The configuration window now reads the mod version from a single shared value.
- Added automated coverage for beta.3 migration and longer status intervals.

## 1.0.0-beta.3 - 2026-07-26

### Highlights

- Added **Smart Recovery**, which preserves the player's original Minecraft settings on disk while a power-saving profile is active.
- BatteryCraft can now restore those settings after an unexpected game shutdown or crash, not only after reconnecting the charger.
- Added the estimated macOS battery time to the optional in-game HUD.

### Profiles and HUD

- Added a configurable transition delay to prevent rapid profile switching when the power state or battery level changes.
- Critical-battery mode still activates immediately for safety.
- Added a configurable HUD refresh interval.
- The HUD now displays the active profile, battery percentage, and estimated remaining time when macOS provides it.

### Interface and diagnostics

- Added automatic English and Spanish localization to the configuration interface.
- Added a diagnostics panel showing macOS detection, CPU architecture, Sodium availability, thermal-warning state, battery information, and pending recovery status.
- Increased the configuration window size to accommodate the new controls and diagnostic information.

### Reliability

- Recovery snapshots are written safely and removed automatically after the original settings have been restored.
- Existing beta.2 configuration files are upgraded automatically with safe defaults.
- Expanded automated coverage for crash recovery, transition stabilization, configuration migration, and macOS battery-output parsing.

### Compatibility

- Minecraft 1.21.x: Java 21
- Minecraft 26.1.x: Java 25
- Minecraft 26.2.x: Java 25
- Fabric API is required.
- BatteryCraft remains a client-side macOS mod and does not require installation on the server.

## 1.0.0-beta.2 - 2026-07-20

- Replaced macOS-unfriendly F8/F9 polling with native Minecraft key mappings.
- Added a BatteryCraft category under the standard key-bindings screen.
- Added freely reassignable controls for cycling profiles and opening configuration.
- Changed defaults to B and O so MacBook media keys are no longer required.
- Added Spanish and English control names.
- Fabric API is now required for reliable cross-version key registration.

## 1.0.0-beta.1 - 2026-07-20

- Added a visual configuration screen opened with F9.
- Added F8 manual profile switching.
- Added configurable notifications and status indicator.
- Added fully editable power profiles.
- Added Sodium detection and optional integration.
- Added experimental macOS thermal-warning handling.
- Added local session statistics.
- Added the official BatteryCraft icon.
- Preserved separate artifacts for Minecraft 1.21.x, 26.1.x and 26.2.x.

## 0.0.1-alpha - 2026-07-20

- Initial public alpha.
- Added macOS battery and charging-state detection.
- Added four automatic power profiles.
- Added restoration of Minecraft settings when external power returns.
- Added separate Fabric artifacts for Minecraft 1.21.x, 26.1.x, and 26.2.x.
