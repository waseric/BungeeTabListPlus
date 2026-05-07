# Requirements — Velocity API 3.5 Upgrade (MC 26.1 Support)

## Scope

**In scope:**
- Upgrade Gradle wrapper from 7.4 to 8.x
- Fix all build script incompatibilities introduced by the Gradle 8 upgrade
- Update Velocity API dependency from `3.4.0-SNAPSHOT` to `3.5.0`
- Register the new `SET_PLAYER_TEAM` packet ID (`0x6D`) for Minecraft 26.1 in `ReflectionUtil`

**Out of scope:**
- Changes to BungeeCord, Waterfall, Bukkit, or Sponge modules
- New BTLP features or configuration options
- Java toolchain version changes (velocity stays at 17, others at 8)

## Decisions

**Gradle 7 → 8 is required before bumping Velocity.** Velocity 3.5.0 does not resolve correctly under Gradle 7; the upgrade must happen first.

**`jcenter()` must be removed.** JCenter was shut down and Gradle 8 warns/errors on it. All `jcenter()` declarations in `build.gradle` and `settings.gradle` must be replaced with `mavenCentral()` or alternative mirrors.

**`ru.vyarus:gradle-quality-plugin` must be re-evaluated.** Version 4.4.0 may not support Gradle 8; a compatible version must be found or the plugin replaced.

**Packet ID is hardcoded via reflection injection.** BTLP injects the `Team` packet into Velocity's internal `StateRegistry` using reflection (Velocity team declined to include it natively). The new Minecraft 26.1 entry follows the same pattern as all prior versions in `ReflectionUtil.injectTeamPacketRegistry()`.

## Context

- Velocity 3.5.0 added support for the Minecraft 26.1 protocol.
- The `SET_PLAYER_TEAM` packet ID for Minecraft 26.1 is `0x6D`.
- The Velocity `ProtocolVersion` enum constant name for MC 26.1 must be verified against the 3.5.0 release (likely `MINECRAFT_26_1`, but confirm from the published API).
- The existing packet mapping table in [ReflectionUtil.java](velocity/src/main/java/codecrafter47/bungeetablistplus/util/ReflectionUtil.java) covers up through `MINECRAFT_1_21_9` (`0x6B`).
- Current `velocityVersion` in [build.gradle](build.gradle) is `3.4.0-SNAPSHOT`; current Gradle wrapper in [gradle-wrapper.properties](gradle/wrapper/gradle-wrapper.properties) is `7.4`.
