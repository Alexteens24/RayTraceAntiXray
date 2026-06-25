# Development

This page covers building, testing, and the technical architecture of this fork. For a full maintainer reference see [FORK.md](https://github.com/Alexteens24/RayTraceAntiXray/blob/main/FORK.md) in the repository.

## Build

```bash
./gradlew build
```

Produces `build/libs/RayTraceAntiXray-<version>.jar` — one universal JAR for Paper 1.21.11 and 26.1.2.

| Area | This fork |
|------|-----------|
| Build system | Gradle multi-module (`build.gradle.kts`, Paperweight) |
| Git layout | Single `main` branch |
| Paper API | Main code: `paperDevBundle` 26.1.2; per-version NMS in `paper_1_21_11` / `paper_26_1_2` |
| Java bytecode | 21 (Gradle toolchain 25) |
| Output JAR | `RayTraceAntiXray-<version>.jar` (no classifier) |

## Tests and benchmarks

```bash
# Unit tests (excludes @Tag("bench"))
./gradlew test

# Section-leap vs pure DDA micro-benchmark (stdout table)
./gradlew bench --rerun-tasks
```

## Local test servers

```bash
./gradlew run1_21_11
./gradlew run26_1_2
```

## Multi-NMS (single JAR)

Version-specific NMS bindings live in Gradle subprojects and are selected at runtime via `NmsBridge`:

| Subproject | Runtime class |
|------------|---------------|
| `paper_1_21_11` | `nms.paper_1_21_11.NmsCompat1_21_11` |
| `paper_26_1_2` | `nms.paper_26_1_2.NmsCompat26_1_2` |

Main code calls `NmsCompat` static methods — never `ChunkPos.pack` / `asLong` directly.

## PacketEvents

This fork uses [PacketEvents](https://modrinth.com/plugin/packetevents) instead of ProtocolLib or custom Netty injection. `PacketListener` handles chunk unload, respawn, and chunk data to align player state with outgoing packets.

## Folia

- `folia-supported: true` in `plugin.yml`
- Ray-trace ticks use `Bukkit.getAsyncScheduler().runAtFixedRate`
- Per-player block updates use region-aware schedulers

## Section leap

When `section-leap: true`, `BlockOcclusionCulling` skips air-only 16³ sections via `SectionRayMath.sectionExitParameter` and `BlockIterator#reseedAfterSectionLeap`. Unit tests in `SectionRayMathTest` and `SectionLeapTraversalTest` assert leap and legacy paths agree.

## Contributing

1. Fork the repository on GitHub.
2. Create a feature branch from `main`.
3. Run `./gradlew test` before opening a PR.
4. Edit documentation under `docs/` for operator-facing changes to commands, permissions, or configuration.
