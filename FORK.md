# Changes in this fork

This document summarizes **how this fork differs** from the classic upstream (Vanillage / stonar96) and related forks—for reviewers and operators.

Upstream goal: RayTraceAntiXray for Paper Anti-Xray **engine-mode 1** with server-side ray tracing.

---

## Build & versions

| Area | This fork |
|------|-----------|
| Build system | **Gradle** multi-module (`build.gradle.kts`, Paperweight), not the older multi-module Maven layout |
| Git layout | Single **`main`** branch; one universal plugin JAR |
| Paper API | Main code: `paperDevBundle` **26.3**; per-version NMS in `paper_26_3` / `paper_26_2` / `paper_26_1_2` / `paper_1_21_11` |
| Java bytecode | **21** (toolchain 25 for Gradle); runs on Java 21+ servers |
| Runtime JAR | **`MOJANG_PRODUCTION`** — Mojang-mapped plugin JAR for Paper 1.20.5+ |
| Output JAR | `RayTraceAntiXray-<version>.jar` (no per-target classifier) |

---

## Multi-NMS (single JAR)

Shared code lives in `RayTraceAntiXray/src/main/java`. Version-specific NMS bindings are in Gradle subprojects:

| Subproject | Runtime class | Bindings |
|------------|---------------|----------|
| **`paper_26_3`** | `nms.paper_26_3.NmsCompat26_3` | `ChunkPos.pack`, `chunkData()`, `ChunkPacketInfo(chunk)`, `onPlayerLeftClickBlock(Level, …)` |
| **`paper_26_2`** | `nms.paper_26_2.NmsCompat26_2` | `ChunkPos.pack`, `chunkPos.x()` / `.z()`, `getChunkData()`, `ChunkPacketInfo(packet, chunk)`, reflected `MinecraftServer.executor` |
| **`paper_26_1_2`** | `nms.paper_26_1_2.NmsCompat26_1_2` | `ChunkPos.pack`, `chunkPos.x()` / `.z()`, `isDisconnected()`, `getChunkData()`, reflected `ServerPlayerGameMode.level` |
| **`paper_1_21_11`** | `nms.paper_1_21_11.NmsCompat1_21_11` | `ChunkPos.asLong`, `chunkPos.x` / `.z`, `processedDisconnect`, `getChunkData()` |

At runtime, `NmsBridge.Holder` detects `ServerBuildInfo.minecraftVersionId()` (fallback `Bukkit.getMinecraftVersion()`) and loads the matching implementation. Main code calls **`NmsCompat`** static methods — never `ChunkPos.pack` / `asLong` / `getChunkData()` directly.

**`BlockState#is(Block)`** is not used; solid-mask init uses **`blockState.getBlock() == Blocks.…`** (works on both targets).

---

## Paper 26.3 — Anti-Xray chunk packet API changes

Paper 26.3 reworked the chunk-packet Anti-Xray entry points. This fork keeps a single universal JAR by
isolating each difference in the NMS subprojects:

1. **`ChunkPacketInfo` construction.** 26.3 takes only the chunk (`new ChunkPacketInfo(chunk)`) and the
   packet is attached afterwards by `ClientboundLevelChunkWithLightPacket` through `setChunkPacket(...)`.
   1.21.11 / 26.1.2 / 26.2 still take `(chunkPacket, chunk)`. The concrete `ChunkPacketInfo` subclass
   therefore lives in each NMS subproject (`ChunkPacketInfoAntiXray26_3`, `…26_2`, `…26_1_2`, `…1_21_11`)
   and is created through `NmsBridge.createChunkPacketInfo`.
2. **`getChunkPacketInfo(...)` signature.** The controller now declares **both** overloads: the 26.3
   `(LevelChunk)` one with `@Override` and the legacy `(ClientboundLevelChunkWithLightPacket, LevelChunk)`
   one without it. Whichever signature the running Paper declares is the one that gets called.
3. **`ClientboundLevelChunkWithLightPacket#getChunkData()` → `chunkData()`.** Reached through
   `NmsBridge.chunkPacketData`, so the block-entity filter keeps working on every target.
4. **`BlockEntityInfo.packedXZ` narrowed from `int` to `byte`.** `Field#getInt` sign-extends it, so the
   block-entity filter masks the value with `0xFF` before unpacking. The masking is a no-op on older
   versions, where the field is still a positive `int`.
5. **`onPlayerLeftClickBlock`** takes `Level` instead of `ServerPlayerGameMode` (see the note above: both
   overloads are declared).

Shared chunk-packet state (target player, nearby chunk cache, obfuscation hand-off) lives in the main
module in `ChunkPacketInfoAntiXrayState`, so the per-version subclasses stay a few lines each.
`ChunkPacketInfoAntiXray` is a plain accessor interface over `ChunkPacketInfo`; every accessor it declares
is identical across all supported versions.

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

- Collects **`ClientboundBlockUpdatePacket`** instances (and optional block-entity packets) for one player update and sends them through **`ServerGamePacketListenerImpl#send`**, preserving Paper's normal outbound pipeline.
- Hidden/visible state is committed only after the connection passes its disconnect check and all packets have been submitted.

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

On [Leaf](https://github.com/Winds-Studio/Leaf), enabling **`async-chunk-send`** in `leaf-global.yml` builds chunk packets on a dedicated async thread. Leaf 1.21.11 and 26.1.2 call **`leaf$modifyBlocks`**; Leaf 26.2 and 26.3 call Paper's standard **`modifyBlocks`** entry point from the async worker. Both variants break Paper’s usual same-thread pairing of **`shouldModify`** → **`getChunkPacketInfo`**.

**`LeafAsyncChunkSendCompat`** (runtime-detected via reflection, no Leaf compile dependency):

- Target queues from **`shouldModify`** (server thread) to **`getChunkPacketInfo`** (async thread), keyed by dimension and chunk column so an out-of-order chunk cannot consume another chunk's player context.
- Multiple sends of the same dimension/chunk remain FIFO. At startup the plugin verifies Leaf's executor is a `ThreadPoolExecutor` with exactly one worker, which is the ordering model used by supported Leaf 1.21.11, 26.1.2, 26.2 and 26.3 builds.
- If Leaf enables async chunk send with an unknown or multi-worker executor, chunk association fails closed and a startup error instructs the operator to disable `async-chunk-send`; Paper Anti-Xray obfuscation still runs, but ray-trace reveal tracking is not assigned to a possibly wrong player.
- Player quit, config reload, and plugin disable remove pending player targets. Empty per-chunk queues are removed atomically.
- **`leaf$modifyBlocks`** (legacy Leaf) and **`modifyBlocks`** (Leaf 26.2+) share the same inline obfuscation path on Leaf's chunk-send thread.

**Paper, Purpur, Folia, Canvas, etc.:** `LeafAsyncChunkSendCompat.isActive()` is always **`false`** (Leaf class absent). Chunk send uses the original **`ThreadLocal`** + **`modifyBlocks`** path only; keyed target queues and Leaf's inline path are never used.

**Leaf with async chunk send disabled:** same as Paper (ThreadLocal + **`modifyBlocks`**).

---

## Runtime dependencies

Besides Paper (and Folia if used), **PacketEvents** (Spigot/Paper build) is required. The **README** remains the primary install guide for server admins.

---

## Acknowledgements

The dirty-tracking approach and supporting data-structure design were inspired by [TauCu's RayTraceAntiXray fork](https://github.com/TauCu/RayTraceAntiXray). This fork adapts those ideas to its multi-version Paper/Leaf architecture.

The Paper 26.3 Anti-Xray API rework (see above) mirrors the change made upstream in
[stonar96/RayTraceAntiXray](https://github.com/stonar96/RayTraceAntiXray) and
[TauCu/RayTraceAntiXray](https://github.com/TauCu/RayTraceAntiXray).

---

## License

Upstream **LICENSE** terms still apply to this source tree; see **LICENSE** and **README** for redistribution rules.
