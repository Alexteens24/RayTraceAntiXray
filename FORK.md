# Changes in this fork

This document summarizes **how this fork differs** from the classic upstream (Vanillage / stonar96) and related forks—for reviewers and operators.

Upstream goal: RayTraceAntiXray for Paper Anti-Xray **engine-mode 1** with server-side ray tracing.

---

## Build & versions

| Area | This fork |
|------|-----------|
| Build system | **Gradle** (`build.gradle.kts`, Paperweight), not the older multi-module Maven layout |
| Git layout | Single **`main`** branch; Paper generation chosen at compile time |
| Paper API | `paperDevBundle`; version from **`-PpaperTarget=`** (`1.21.11` or `26.1.2`) |
| Java toolchain | **21** for `paperTarget=1.21.11`; **25** for `paperTarget=26.1.2` |
| Runtime JAR | **`MOJANG_PRODUCTION`** — Mojang-mapped plugin JAR for Paper 1.20.5+ |
| Output JAR | `RayTraceAntiXray-<version>-<paperTarget>.jar` (Gradle classifier) |

---

## Build targets (`paperTarget`)

Shared code lives in `RayTraceAntiXray/src/main/java`. Paper-specific NMS is compiled from one of:

| `paperTarget` | NMS sources | `NmsCompat` bindings |
|---------------|-------------|----------------------|
| **`1.21.11`** | `src/nms/paper-1.21.11/java` | `ChunkPos.asLong`, `chunkPos.x` / `.z`, `processedDisconnect`, … |
| **`26.1.2`** | `src/nms/paper-26.1.2/java` | `ChunkPos.pack`, `chunkPos.x()` / `.z()`, `isDisconnected()`, … |

Main code must call **`NmsCompat`** for these APIs — never `ChunkPos.pack` / `asLong` directly.

**`BlockState#is(Block)`** is not used; solid-mask init uses **`blockState.getBlock() == Blocks.…`** (works on both targets).

Default local build: `paperTarget=26.1.2` in `gradle.properties`.

---

## Packets — PacketEvents instead of ProtocolLib / custom Netty injection

- **`plugin.yml`**: `depend: packetevents`
- **`PacketListener`**: uses **PacketEvents** for chunk unload, respawn, and chunk data to align player state with outgoing packets.
- This fork does **not** embed a duplex Netty handler like some other forks: you must install the **PacketEvents** plugin on the server.

---

## Ray-trace scheduling

- Ray-trace ticks use **`Bukkit.getAsyncScheduler().runAtFixedRate`** (Paper / Folia), not `java.util.Timer` like some upstream builds.

---

## Folia

- Detects Folia at runtime (`RegionizedServer`).
- **`PacketListener`**: on Folia, defers chunk/unload/respawn handling to **`player.getScheduler()`** so region ownership rules are respected.
- **`plugin.yml`**: `folia-supported: true`
- Per-player block updates use the appropriate scheduler (see `PlayerListener` / `UpdateBukkitRunnable`).

---

## Block updates to clients (`UpdateBukkitRunnable`)

- Batches multiple **`ClientboundBlockUpdatePacket`** instances (and optional block-entity packets) into **one** Netty event-loop task: sequential **`write`**, then a **single `flush`**, instead of per-packet **`writeAndFlush`** from the game thread.
- The Netty runnable only writes when **`conn.connection.channel`** is the **same instance** as the channel used to schedule—avoids writing on the wrong pipeline / wrong thread.

---

## Worlds — hook controller for worlds loaded before the plugin enables

- Paper creates worlds **before** plugins enable; **`WorldInitEvent` may have already fired**.
- **`WorldListener.handleLoad(plugin, world)`** is also invoked for **`Bukkit.getWorlds()`** from **`onEnable`**, in addition to **`WorldInitEvent`**.

---

## Ray trace — behaviour & logging

- **`RayTraceTimerTask`**: logs **`RejectedExecutionException`** while the plugin is still **running**; catches stray **`Throwable`** around **`invokeAll`**.
- **`RayTraceCallable`**: on failure inside **`rayTrace()`**, **logs only** (does not rethrow after logging) so failures are not doubled unnecessarily.

---

## Section leap (air-only section skipping)

When **`section-leap: true`** (opt-in; default is **`false`**):

1. During DDA in **`BlockOcclusionCulling`**, if **`BlockOcclusionGetter#sectionHasOnlyAir`** is true for the current voxel’s 16³ section, **`SectionRayMath.sectionExitParameter`** computes the ray exit of that section.
2. **`BlockIterator#reseedAfterSectionLeap`** continues DDA from just inside that exit instead of visiting every air block in the section.
3. **`RayTraceCallable`** implements **`sectionHasOnlyAir`** via **`LevelChunkSection#hasOnlyAir()`** on loaded chunks only; unloaded or missing sections return **`false`** (conservative — no leap).

Legacy per-voxel traversal is the default. Set **`section-leap: true`** to enable air-only section skipping. Unit tests in **`SectionRayMathTest`** and **`SectionLeapTraversalTest`** assert leap and legacy paths agree on mock getters.

Micro-benchmark (DDA only vs section-leap, prints a table): **`./gradlew bench --rerun-tasks`**. Normal **`./gradlew test`** excludes `@Tag("bench")` tests.

---

## Config reload (`/raytraceantixray reload`)

This fork supports reloading **`config.yml` at runtime** (upstream historically did not). **`RayTraceAntiXray.reloadPluginConfiguration()`** (main thread only):

1. Reloads **`config.yml`** from disk.
2. Shuts down and recreates the ray-trace **thread pool** and **async tick** (`ms-per-ray-trace-tick`, `ray-trace-threads`, `update-ticks`).
3. Reapplies **`ChunkPacketBlockControllerAntiXray`** on all loaded worlds via **`WorldListener.handleLoad`**.
4. Clears and re-registers **online players** (`PlayerListener.unregisterAndReregisterAll`).

**Still unsafe / unsupported:** Bukkit **`/reload`**, hot-swapping the plugin JAR, or enable/disable via plugin managers. Use a **full server restart** for binary or dependency changes (Paper Anti-Xray, PacketEvents).

**Operator notes:** if obfuscation looks wrong after reload, have players **reconnect**. Per-chunk hidden-block lists are built when a chunk is **sent**, not on reload.

Command permissions and usage strings: **`plugin.yml`**, **`README.txt`** (saved to the data folder on first run).

---

## Leaf — `async-chunk-send`

On [Leaf](https://github.com/Winds-Studio/Leaf), enabling **`async-chunk-send`** in `leaf-global.yml` builds chunk packets on a dedicated async thread and calls **`leaf$modifyBlocks`** instead of **`modifyBlocks`**. That breaks Paper’s usual same-thread pairing of **`shouldModify`** → **`getChunkPacketInfo`**.

**`LeafAsyncChunkSendCompat`** (runtime-detected via reflection, no Leaf compile dependency):

- FIFO queue from **`shouldModify`** (server thread) to **`getChunkPacketInfo`** (async thread), keyed by chunk column.
- **`ChunkPacketBlockControllerAntiXray#leaf$modifyBlocks`** runs obfuscation inline on Leaf’s chunk-send thread (same approach as Paper’s Leaf patch).

**Paper, Purpur, Folia, Canvas, etc.:** `LeafAsyncChunkSendCompat.isActive()` is always **`false`** (Leaf class absent). Chunk send uses the original **`ThreadLocal`** + **`modifyBlocks`** path only; the FIFO queue and **`leaf$modifyBlocks`** are never used (`leaf$modifyBlocks` is not called by stock Paper).

**Leaf with async chunk send disabled:** same as Paper (ThreadLocal + **`modifyBlocks`**).

---

## Runtime dependencies

Besides Paper (and Folia if used), **PacketEvents** (Spigot/Paper build) is required. The **README** remains the primary install guide for server admins.

---

## License

Upstream **LICENSE** terms still apply to this source tree; see **LICENSE** and **README** for redistribution rules.
