# Validation — Velocity API 3.5 Upgrade (MC 26.1 Support)

## Checks

- [x] `./gradlew build` completes successfully with Gradle 8.x (no errors, no unresolved dependencies) — **Gradle 8.14.4, 132 tests, 0 failures**
- [x] No `jcenter()` references remain in any `build.gradle` or `settings.gradle` file
- [ ] `velocityVersion` in `build.gradle` is set to `3.5.0` (not a SNAPSHOT) — *currently 3.4.0 (stable); will be updated in TG2*
- [ ] `ReflectionUtil.injectTeamPacketRegistry()` contains a mapping for Minecraft 26.1 with packet ID `0x6D`
- [ ] `ReflectionUtil.injectTeamPacketRegistry()` contains a mapping for Minecraft 1.21.11 with the correct packet ID (to be determined from Velocity 3.5.0 sources)
- [ ] All new `ProtocolVersion` constant names match those exported by Velocity 3.5.0
- [x] BungeeCord/Waterfall/Bukkit/Sponge modules are unaffected (their builds still pass)
- [ ] BTLP tab list displays correctly on a Velocity server with a Minecraft 26.1 client connected
