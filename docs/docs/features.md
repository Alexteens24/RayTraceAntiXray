# Features

## Background

Paper Anti-Xray in **engine-mode 1** replaces hidden blocks (e.g. ores) with decoy blocks (stone, deepslate, etc.) inside chunk packets. That mechanism targets blocks **not** adjacent to air. Ores on cave walls or in open pockets remain visible in the raw chunk data.

RayTraceAntiXray closes this gap by:

1. Identifying trace candidates when a chunk is prepared for a player.
2. Obfuscating those positions in the outgoing packet (same decoy blocks as Paper).
3. Running asynchronous visibility tests from the player's eye (and optionally third-person origins).
4. Sending **block-update** packets to reveal only blocks with an unobstructed line of sight.

<img class="showcase-shot" src="https://user-images.githubusercontent.com/18699205/185815590-4b2efce6-5a26-4579-b079-e9958a454fd0.gif" alt="Demonstration: exposed ores obfuscated until line-of-sight is established" />

## Pipeline

| Step | What happens |
|------|--------------|
| **Chunk send** | Record world positions of configured block types exposed to air (subject to per-chunk cap and Paper height limits). |
| **Obfuscate** | Write decoy block states into the chunk palette before the client receives it. |
| **Ray trace** | On a configurable schedule, a thread pool traces rays from each player toward queued blocks within `ray-trace-distance`. |
| **Reveal** | Visibility results are batched into `ClientboundBlockUpdatePacket` and flushed to the client. |

## Ray casting stages

| Stage | Description |
|--------|-------------|
| **Frustum culling** | Optional early reject using view direction. |
| **Section leap** | If `section-leap: true` and a 16³ section reports air-only, skip voxels analytically and resume DDA at the section boundary. |
| **Voxel traversal** | Amanatides–Woo DDA steps along the ray. |
| **Occlusion** | Solid blocks and adjacent-face checks determine obstruction. |
| **Conservative bias** | Ambiguous or unloaded geometry is treated as occluding; when culling is uncertain, visibility errs toward **revealed** rather than falsely hidden. |

## Rehide (optional)

With `rehide-blocks: true`, blocks that leave the visible set (subject to `rehide-distance`) can be obfuscated again without waiting for a full chunk resend.

## Key components

| Component | Role |
|-----------|------|
| `ChunkPacketBlockControllerAntiXray` | Hooks Paper chunk obfuscation; enqueues per-player block lists. |
| `PacketListener` | Aligns player state with outgoing chunk / unload / respawn packets (PacketEvents). |
| `RayTraceTimerTask` | Paper async scheduler tick; runs ray work on the thread pool. |
| `UpdateBukkitRunnable` | Drains visibility results; batches block updates on the player scheduler (Folia-safe). |

**Runtime dependencies:** Paper (Folia-capable builds), [PacketEvents](https://modrinth.com/plugin/packetevents), Paper Anti-Xray **engine-mode 1**.

Anonymous usage metrics via [bStats](https://bstats.org/plugin/bukkit/RayTraceAntiXray/31528) and [FastStats](https://faststats.dev). Opt out of bStats with `plugins/bStats/config.yml`; opt out of FastStats with the JVM flag `-Dfaststats.enabled=false`.

## Limitations

- **CPU cost** scales with player count, `ray-trace-threads`, blocks per chunk, distance, and third-person mode. Dedicated spare cores are recommended.
- **Culling is approximate** by design; conservative visibility reduces false hides at the cost of occasional early reveals under load.
- **Static per-send block lists** until chunk resend; mining or placing ores does not update the trace set immediately.
- **Section leap** depends on `hasOnlyAir()`; false negatives skip optimization only — false positives are avoided by conservative section queries on unloaded data.
