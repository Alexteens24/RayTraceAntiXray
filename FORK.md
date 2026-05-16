# Changes in this fork

This document summarizes **how this fork differs** from the classic upstream (Vanillage / stonar96) and related forks—for reviewers and operators.

Upstream goal: RayTraceAntiXray for Paper Anti-Xray **engine-mode 1** with server-side ray tracing.

---

## Build & versions

| Area | This fork |
|------|-----------|
| Build system | **Gradle** (`build.gradle.kts`, Paperweight), not the older multi-module Maven layout |
| Paper API | `paperDevBundle`; default version via **`paperVersion`** (see `gradle.properties` or `-PpaperVersion=...`) |
| Java | **21** on **`1.21.11`** (and 1.21.x feature branches); **25** on **`26.1.2`** (Paper 26.1+ userdev) |
| Runtime JAR | **`MOJANG_PRODUCTION`** — Mojang-mapped plugin JAR for Paper 1.20.5+ |

---

## Version branches

The fork keeps **two Paper generations** as **git branches** so NMS and toolchain can diverge without a single “one size fits all” tree.

| Branch | Paper / Minecraft (typical) | Java (Gradle) | `plugin.yml` `api-version` | Notes |
|--------|-----------------------------|---------------|----------------------------|--------|
| **`1.21.11`** | `paperVersion` e.g. `1.21.11-R0.1-SNAPSHOT` (`gradle.properties`) | **21** | `1.21.11` | Release line for **Minecraft 1.21.11** / Paper 1.21.11 servers. |
| **`26.1.2`** | e.g. `26.1.2.build.63-stable` | **25** | `'26.1.2'` | One dedicated commit retargets NMS for the 26.x line. |

### 26.1.2 — code deltas vs `1.21.11`

Mechanical API renames only:

- **`ChunkPos`**: `asLong(x, z)` → **`pack(x, z)`**; **`toLong()`** on chunk pos → **`pack()`**.
- **`ChunkPos` coordinates**: field access **`.x` / `.z`** → **`.x()` / `.z()`** (record-style accessors on Mojang types).
- **`ServerGamePacketListenerImpl`**: **`processedDisconnect`** → **`isDisconnected()`** in `UpdateBukkitRunnable`.
- **`build.gradle.kts` / `gradle.properties`**: toolchain and **`options.release`** set to **25**; default **`paperVersion`** / **`minecraftVersion`** point at the **26.1.2** line.

Check out the branch that matches your server’s Paper generation before building.

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

## Runtime dependencies

Besides Paper (and Folia if used), **PacketEvents** (Spigot/Paper build) is required. The **README** remains the primary install guide for server admins.

---

## License

Upstream **LICENSE** terms still apply to this source tree; see **LICENSE** and **README** for redistribution rules.
