# Plan — Velocity API 3.5 Upgrade (MC 26.1 Support)

Enable BungeeTabListPlus to run on Velocity with Minecraft 26.1 by upgrading Gradle to 8.x, updating Velocity API to 3.5.0, and registering the new SET_PLAYER_TEAM packet ID.

---

## Task Group 1 — Upgrade Gradle Wrapper to 8.x ✅ COMPLETE

1. ✅ Update `gradle/wrapper/gradle-wrapper.properties` — upgraded `distributionUrl` to Gradle **8.14.4** (latest stable 8.x; Java 25 support requires 8.14+).
2. ✅ Replace all `jcenter()` repository declarations — replaced in root `build.gradle` (buildscript block), `settings.gradle`, `minecraft-data-api/build.gradle`, `minecraft-data-api/settings.gradle`, `TabOverlayCommon/build.gradle`, and `TabOverlayCommon/settings.gradle`. Dead repos (`papermc.io/maven-snapshots`, `nexus.prgm.in`) also removed.
3. ✅ Remove `ru.vyarus:gradle-quality-plugin` — no Gradle 8-compatible version exists; removed the classpath entry and all `apply plugin: 'ru.vyarus.quality'` calls from all subprojects.
4. ✅ Fix remaining Gradle 8 API breaks:
   - Added `org.gradle.toolchains.foojay-resolver-convention 0.8.0` to all three `settings.gradle` files to auto-provision JDKs (Java 8 toolchain needed but not locally installed).
   - Migrated all Shadow plugin usages from `com.github.johnrengelman.shadow 7.1.2/5.2.0` to `com.gradleup.shadow 8.3.0` (old version referenced the removed `MavenPlugin` class).
   - Fixed `velocity/build.gradle`: `velocity-proxy` is not published to Maven; replaced `compileOnly "com.velocitypowered:velocity-proxy:..."` with a download task that fetches the Velocity server JAR from PaperMC's downloads API. Changed `velocityVersion` from `3.4.0-SNAPSHOT` to `3.4.0` (stable) to get a known build.
   - Fixed dead repo for BungeePerms in `minecraft-data-api/bungee/bungeeperms3/build.gradle` — changed URL to `nexus.codecrafter47.de/content/repositories/thirdparty/`.
   - Added `--add-opens java.desktop/java.awt=ALL-UNNAMED` and `--add-opens java.desktop/java.awt.color=ALL-UNNAMED` to `bungee/build.gradle` test JVM args (Java 25 module system blocks Gson reflection on `java.awt.Color` / `java.awt.color.ColorSpace`).
   - Fixed BungeeCord API breaking changes in test sources across 5 files:
     - `Either<String,X>` fields on `Team` packet: added `.getLeft()` for getters, `Either.left(...)` for setters, with null guards for mode-2 packets that don't populate all fields.
     - `PlayerListItem.Item.getDisplayName()` now returns `BaseComponent` instead of `String`: wrapped with `ComponentSerializer.toString()` or `toPlainText()` / `toLegacyText()` as appropriate.
     - `AbstractTabOverlayHandler` constructor changed from 5 → 7 args (added `is119OrLater`, `is1215OrLater`): added `false, false` in test mock.
     - `PacketHandler` interface gained `onPlayerListRemovePacket` and `onPlayerListUpdatePacket`: added stub overrides returning `PacketListenerResult.PASS`.
     - `TestRealWorldExamples`: registered a Gson `TypeAdapterFactory` for `net.md_5.bungee.protocol.util.Either` so log-file JSON (plain strings) deserialises into `Either.left(value)`.
   - Fixed production code `AbstractLegacyTabOverlayHandler.getName()`: changed `toLegacyText()` → `toPlainText()` to avoid `§f` being prepended by BungeeCord's new default-white-color behavior.
5. ✅ `./gradlew build` completes cleanly — 132 tests pass, 0 failures.

## Task Group 2 — Update Velocity API Dependency to 3.5.0

6. Update `velocityVersion` in `build.gradle` from `3.4.0` to `3.5.0`.
7. Resolve any compilation errors in the `velocity` module caused by API changes between 3.4 and 3.5.
8. Verify the `ProtocolVersion` constant name for Minecraft 26.1 in the Velocity 3.5.0 API (e.g., `MINECRAFT_26_1`).

## Task Group 3 — Register MC 26.1 Packet ID

9. Add a `PacketMapping` entry for Minecraft 26.1 with ID `0x6D` in `ReflectionUtil.injectTeamPacketRegistry()` in `velocity/src/main/java/codecrafter47/bungeetablistplus/util/ReflectionUtil.java`.
10. Confirm the constant name found in Task 8 is used correctly in the new mapping.
11. Look up the `SET_PLAYER_TEAM` packet ID for Minecraft 1.21.11 from the Velocity 3.5.0 source or protocol changelog, and add the corresponding `PacketMapping` entry and `ProtocolVersion` constant to `ReflectionUtil.injectTeamPacketRegistry()`.
