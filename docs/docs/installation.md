# Installation

## Requirements

| Requirement | Specification |
|-------------|---------------|
| **Minecraft / Paper** | 1.21.11 or 26.1.2 |
| **Server Java** | 21+ (25 recommended for 26.x) |
| **Server software** | [Paper](https://papermc.io/downloads/paper) or Folia-capable Paper fork |
| **Paper Anti-Xray** | Enabled with **`engine-mode: 1`** ([documentation](https://docs.papermc.io/paper/anti-xray/)) |
| **PacketEvents** | Required — [Modrinth](https://modrinth.com/plugin/packetevents) or Spigot/Paper build |

::: warning PacketEvents is required
RayTraceAntiXray declares `depend: packetevents` in `plugin.yml`. The server will not start the plugin without PacketEvents installed.
:::

::: warning Do not use /reload
Do **not** use Bukkit `/reload`, PlugMan-style hot plug, or enable/disable the JAR on a running server. Chunk controllers and per-player state will desynchronize. Use a **full server restart** after installing or replacing the plugin JAR.
:::

## Installation steps

### 1. Download

Get the JAR from [BuiltByBit](https://builtbybit.com/resources/raytraceantixray.24914/) or [build from source](/docs/download).

### 2. Install dependencies

1. **Stop your server** completely.
2. Install [PacketEvents](https://modrinth.com/plugin/packetevents) into `plugins/`.
3. Enable Paper Anti-Xray with **`engine-mode: 1`** in `paper-world-defaults.yml` or per-world config ([Paper docs](https://docs.papermc.io/paper/anti-xray/)).

### 3. Install RayTraceAntiXray

1. Place `RayTraceAntiXray-<version>.jar` in `plugins/`.
2. **Start the server** (full restart, not `/reload`).

### 4. Configure

Edit `plugins/RayTraceAntiXray/config.yml`. See the [Configuration](/docs/configuration) page for all options.

On first run the plugin also writes `plugins/RayTraceAntiXray/README.txt` with command and permission reference.

### 5. Verify

Run `/plugins` in console. RayTraceAntiXray should appear with a green status alongside PacketEvents.

## Tuning reference

Recommended settings for production servers: [stonar96's settings gist](https://gist.github.com/stonar96/69ca0311392188b7ac2ece226286147f).

## Updating

1. **Stop** the server.
2. **Replace** the old `.jar` with the new one.
3. **Start** the server (full restart).

Use `/raytraceantixray reload` only for `config.yml` changes — not for JAR or dependency updates.

## Generated files

| File | Description |
|------|-------------|
| `config.yml` | Main plugin configuration |
| `README.txt` | In-server command and permission cheat sheet |
