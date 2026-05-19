# RayTraceAntiXray
Paper plugin for server-side async multithreaded ray tracing to hide ores that are exposed to air using Paper Anti-Xray engine-mode 1.

Paper Anti-Xray can't hide ores that are exposed to air in caves for example (see picture below). This plugin is an add-on for Paper Anti-Xray to hide those ores too, using ray tracing to calculate whether or not those ores are visible to players. This plugin can also fully hide block entities such as chests since Minecraft 1.20.6.

![RayTraceAntiXray](https://user-images.githubusercontent.com/18699205/185815590-4b2efce6-5a26-4579-b079-e9958a454fd0.gif)

## What's changed in this fork

This repository is a fork of **[stonar96/RayTraceAntiXray](https://github.com/stonar96/RayTraceAntiXray)**. Upstream ships a **Maven** layout, depends on **ProtocolLib** for packet integration, and uses a **`java.util.Timer`**-style driver for ray-trace ticks in its current tree. This fork keeps the same overall goal (Paper Anti-Xray **engine-mode 1** + server-side ray tracing) but changes how the project is built and how it talks to the server runtime. Day-to-day work targets **`26.1.2`** (Paper 26.x) and **`1.21.11`** (Paper 1.21.11); this repo does **not** use a `main` branch.

**Highlights**

* **Build**: **Gradle** + Paperweight (`build.gradle.kts`); Java **21** on **`1.21.11`** (and feature branches on that line), Java **25** on **`26.1.2`**; Mojang-mapped plugin JAR (`MOJANG_PRODUCTION`).
* **Packets**: **PacketEvents** instead of ProtocolLib (see `plugin.yml`); no embedded duplex Netty handler for chunk lifecycle—install the PacketEvents plugin on the server.
* **Folia / threading**: Ray-trace work is driven from **Paper’s async scheduler** (`runAtFixedRate`); Folia is detected at runtime and sensitive paths defer to **region/player schedulers** where required.
* **Client updates**: `UpdateBukkitRunnable` **batches** block-update packets and uses a **single flush** per batch, with checks so writes stay on the expected channel.
* **World hookup**: Worlds that already exist when the plugin enables still get a controller (not only `WorldInitEvent`).
* **Reliability & ops**: Clearer **shutdown** and **player disconnect** handling, improved **logging** around ray-trace pool failures, **cache initialization** tweaks in the hot ray path, and **`/raytraceantixray reload`** to apply `config.yml` without a full server restart.
* **Ray traversal**: Per-world **`section-leap`** in `config.yml` skips voxel steps across **air-only 16³ chunk sections** (`LevelChunkSection#hasOnlyAir()`) before block DDA; set `false` to force legacy per-voxel traversal (useful in profilers).

**Paper / Minecraft version branches**

* **`1.21.11`** — tracks the **Minecraft 1.21.11** line (Paper `1.21.11-*`, Java **21**, `api-version: 1.21.11`).
* **`26.1.2`** — targets **Paper 26.1.2** / the newer **26.x** API (`api-version: '26.1.2'`), **Java 25** toolchain, and small **NMS renames** in the plugin (e.g. `ChunkPos.pack` vs `asLong`, `ChunkPos` coordinate accessors, `ServerGamePacketListenerImpl.isDisconnected()`). Build defaults live in `gradle.properties` on that branch.

See **[FORK.md — Version branches](FORK.md#version-branches)** for a concise diff-style list.

For a longer, file-by-file rationale (including notes that mirror upstream’s LICENSE expectations), see **[FORK.md](FORK.md)**.

## How to install
* Download and install [Paper](https://papermc.io/downloads/paper) **26.1.2** (match `paperVersion` / `minecraftVersion` in `gradle.properties`; use **JDK 25** to build this branch). Follow Paper’s release notes for Folia support on this generation.
* Enable [Paper Anti-Xray](https://docs.papermc.io/paper/anti-xray/) using `engine-mode: 1`.
* Download and install [PacketEvents](https://modrinth.com/plugin/packetevents) (Spigot/Paper build).
* Download and install [RayTraceAntiXray](https://builtbybit.com/resources/raytraceantixray.24914/). (For older Minecraft versions, browse the update history.)
* Configure RayTraceAntiXray by editing the file plugins/RayTraceAntiXray/[config.yml](RayTraceAntiXray/src/main/resources/config.yml).
* See also: [Recommended settings](https://gist.github.com/stonar96/69ca0311392188b7ac2ece226286147f).
* **Restart the server** after the first install or after replacing the plugin JAR. Do **not** use Bukkit’s `/reload`, PlugMan-style plugin managers, or enable/disable the plugin jar on a running server — that leaves chunk controllers and player state inconsistent.

## Commands

After the plugin is enabled, you can change most RayTraceAntiXray settings without a full restart:

| Command | Permission | Description |
|---------|------------|-------------|
| `/raytraceantixray reload` | `raytraceantixray.command.raytraceantixray.reload` | Reloads `config.yml`, restarts the ray-trace thread pool and async tick, reapplies per-world chunk controllers, and re-registers online players. |
| `/raytraceantixray timings on` | `raytraceantixray.command.raytraceantixray.timings.on` | Enables internal timings (also requires `…timings`). |
| `/raytraceantixray timings off` | `raytraceantixray.command.raytraceantixray.timings.off` | Disables timings (also requires `…timings`). |

All subcommands require `raytraceantixray.command.raytraceantixray`. Full permission names are listed in `plugins/RayTraceAntiXray/README.txt` (copied from the jar on first run).

**After `/raytraceantixray reload`:** players who see missing or stuck obfuscation should reconnect. Chunks already sent to clients are not rebuilt until those chunks are sent again (same as upstream — block lists are fixed when a chunk is first sent).

## Known issues
* Depending on the number of players and config settings, this plugin can be resource intensive. I only recommend using it if you have "unused" CPU threads available on your server in order to minimize the impact on the main thread.
* The culling algorithm is intentionally not 100% accurate for performance and functional reasons. When in doubt, it is assumed that a block is visible. Thus hidden blocks tend to be revealed rather earlier than late, provided that the server isn't overloaded and doesn't lag. Usually, however, this cannot be abused.
* Config reload (`/raytraceantixray reload`) does not replace a server restart when you change the plugin binary, Paper Anti-Xray settings, or PacketEvents — restart for those.
## Demo
![RayTraceAntiXray](https://user-images.githubusercontent.com/18699205/112784731-aed75e00-9052-11eb-92d6-b0dd4af79290.gif)
## License
The [LICENSE](LICENSE) file applies to the **source code** of this project. Please don't (re)distribute **compiled binary versions** of this project or derivative works that are directly usable as intended by this project. Shading or using this project as a library for other purposes is permitted.
