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

Get the JAR from [Modrinth](https://modrinth.com/plugin/forkedraytraceantixray).

### 2. Install dependencies

1. **Stop your server** completely.
2. Install [PacketEvents](https://modrinth.com/plugin/packetevents) into `plugins/`.
3. Enable Paper Anti-Xray with **`engine-mode: 1`** — use the [official Paper preset](https://docs.papermc.io/paper/anti-xray/#engine-mode-1) in `config/paper-world-defaults.yml` (see [Recommended setup](/docs/recommended-configuration)).

### 3. Install RayTraceAntiXray

1. Place `RayTraceAntiXray-<version>.jar` in `plugins/`.
2. **Start the server** (full restart, not `/reload`).

### 4. Configure

Edit `plugins/RayTraceAntiXray/config.yml`. Copy-paste presets from [Recommended setup](/docs/recommended-configuration) (Paper Anti-Xray + plugin). See [Configuration](/docs/configuration) for every key.

On first run the plugin also writes `plugins/RayTraceAntiXray/README.txt` with command and permission reference.

### 5. Verify

Run `/plugins` in console. RayTraceAntiXray should appear with a green status alongside PacketEvents.

## Tuning reference

Production presets (Paper + plugin): [Recommended setup](/docs/recommended-configuration).

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
