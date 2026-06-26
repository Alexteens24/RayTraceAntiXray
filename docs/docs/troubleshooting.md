# Troubleshooting

## Obfuscation looks wrong after reload

`/raytraceantixray reload` reapplies configuration and re-registers players, but **per-chunk hidden-block lists are built when a chunk is sent**, not on reload.

- Have affected players **reconnect** or re-enter the area so chunks are resent.
- If problems persist after a JAR or dependency change, perform a **full server restart**.

## When reload is not enough

Use a **full server restart** for:

- Replacing the RayTraceAntiXray JAR
- Updating PacketEvents
- Changing Paper Anti-Xray settings or engine mode
- Any PlugMan-style enable/disable of the plugin

::: warning Never use Bukkit /reload
Bukkit `/reload` tears down and recreates plugin state in ways that desynchronize chunk controllers. Always stop and start the server process instead.
:::

## Static per-send block lists

The list of blocks to ray-trace is fixed when a chunk packet is built for a player. Newly placed or broken ores are **not** reflected until the chunk is sent again (e.g. player leaves and re-enters render distance).

## Leaf — async-chunk-send

On [Leaf](https://github.com/Winds-Studio/Leaf), enabling **`async-chunk-send`** in `leaf-global.yml` builds chunk packets on a dedicated async thread. RayTraceAntiXray detects this at runtime via `LeafAsyncChunkSendCompat` and uses a FIFO queue to pair obfuscation with async chunk sends.

- **Paper, Purpur, Folia, Canvas, etc.:** compat layer is inactive; standard ThreadLocal + `modifyBlocks` path only.
- **Leaf with async chunk send disabled:** same as stock Paper.

No extra configuration is required — detection is automatic when the Leaf class is present.

## High CPU usage

Ray tracing cost scales with:

- Online player count
- `ray-trace-threads`
- `max-ray-trace-block-count-per-chunk`
- `ray-trace-distance`
- `ray-trace-third-person` (significantly more expensive)

Reduce these values or dedicate spare CPU cores so the main thread stays responsive. Enable timings with `/raytraceantixray timings on` to inspect per-tick batch duration in the console.

## Metrics opt-out

Anonymous usage metrics are sent via [bStats](https://bstats.org/plugin/bukkit/RayTraceAntiXray/31528) and [FastStats](https://faststats.dev).

- **bStats:** opt out in `plugins/bStats/config.yml` on the server.
- **FastStats:** disable transmission with the JVM flag `-Dfaststats.enabled=false` when starting the server (see [FastStats system properties](https://docs.faststats.dev/java/system-properties)).

## Getting help

1. Check server console logs for errors during startup and ray-trace ticks.
2. Confirm PacketEvents is loaded and Paper Anti-Xray uses **engine-mode 1**.
3. Open an issue on [GitHub](https://github.com/Alexteens24/RayTraceAntiXray/issues) with your Paper version, config, and relevant log excerpts.
