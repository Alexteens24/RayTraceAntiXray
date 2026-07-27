# Recommended configuration

Configure **Paper Anti-Xray first**, restart the server, then edit **RayTraceAntiXray** `config.yml`. This plugin requires Paper **`engine-mode: 1`** — the same mode [Paper documents as recommended](https://docs.papermc.io/paper/anti-xray/) for the lightweight obfuscation pipeline.

Paper's FAQ states that with engine-mode 1, **ores exposed to air** (caves, lakes) are **not** hidden by Paper alone. RayTraceAntiXray adds server-side ray tracing for those cases.

Plugin presets below are adapted from [stonar96's recommended settings](https://gist.github.com/stonar96/69ca0311392188b7ac2ece226286147f). Adjust Bukkit world names in `world-settings` (`world_nether`, `world_the_end`, …) to match your server.

::: warning Restart after Paper changes
Paper requires a **full server restart** after Anti-Xray changes — not `/reload`. Same for installing or replacing the plugin JAR. Use `/raytraceantixray reload` only for plugin `config.yml` tuning on an already-running server.
:::

## Setup order

1. Apply Paper Anti-Xray presets (below) → **restart**
2. Install [PacketEvents](https://modrinth.com/plugin/packetevents) + RayTraceAntiXray
3. Paste a plugin preset into `plugins/RayTraceAntiXray/config.yml` → **restart** (or `/raytraceantixray reload` for plugin-only edits)

---

## Paper Anti-Xray setup

RayTraceAntiXray checks loaded worlds at startup. If ray tracing is enabled for a world but Paper Anti-Xray is disabled or not using `engine-mode: 1`, the plugin warns the console and sends operators a clickable setup reminder when they join.

The reminder does not modify Paper configuration. After applying the settings below, perform a **full server restart**; `/raytraceantixray reload` cannot reload Paper Anti-Xray.

Source: [Paper — Configuring Anti-Xray](https://docs.papermc.io/paper/anti-xray/#engine-mode-1)

### Overworld (default)

File: **`config/paper-world-defaults.yml`**

Replace the existing `anticheat.anti-xray` block with:

```yaml
anticheat:
  anti-xray:
    enabled: true
    engine-mode: 1
    hidden-blocks:
    # There's no chance to hide dungeon chests as they are entirely surrounded by air,
    # but buried treasures will be hidden.
    - chest
    - coal_ore
    - deepslate_coal_ore
    - copper_ore
    - deepslate_copper_ore
    - raw_copper_block
    - diamond_ore
    - deepslate_diamond_ore
    - emerald_ore
    - deepslate_emerald_ore
    - gold_ore
    - deepslate_gold_ore
    - iron_ore
    - deepslate_iron_ore
    - raw_iron_block
    - lapis_ore
    - deepslate_lapis_ore
    - redstone_ore
    - deepslate_redstone_ore
    lava-obscures: false
    # As of 1.18 some ores are generated much higher.
    # Please adjust the max-block-height setting at your own discretion.
    # https://minecraft.wiki/w/Ore might be helpful.
    max-block-height: 64
    # The replacement-blocks list is not used in engine-mode: 1.
    # Changing this will have no effect.
    replacement-blocks: []
    update-radius: 2
    use-permission: false
```

### Nether

File: **`world/dimensions/minecraft/the_nether/paper-world.yml`**

```yaml
anticheat:
  anti-xray:
    enabled: true
    engine-mode: 1
    hidden-blocks:
    - ancient_debris
    - nether_gold_ore
    - nether_quartz_ore
    lava-obscures: false
    max-block-height: 128
    replacement-blocks: []
    update-radius: 2
    use-permission: false
```

### The End

File: **`world/dimensions/minecraft/the_end/paper-world.yml`**

```yaml
anticheat:
  anti-xray:
    enabled: false
```

::: info engine-mode must be 1
RayTraceAntiXray hooks Paper's **engine-mode 1** chunk controller. Modes 2 and 3 are **not** supported by this plugin.
:::

::: tip max-block-height
Paper's recommended overworld value is **`64`**. Raise it only if you need coverage at higher Y levels — more blocks for Paper (and ray tracing) to process.
:::

---

## RayTraceAntiXray — recommended (more protection)

**Stronger protection for air-exposed blocks.** Higher CPU — `ray-trace-third-person: true` is roughly **3×** more expensive.

File: **`plugins/RayTraceAntiXray/config.yml`**

`ray-trace-blocks` lists block types to obfuscate and ray-trace when **exposed to air**. Align with Paper `hidden-blocks` plus any extras you care about (e.g. `spawner`, `mossy_cobblestone`).

```yaml
settings:
  anti-xray:
    update-ticks: 1
    ms-per-ray-trace-tick: 50
    # Adjust to available (ideally unused) CPU threads.
    ray-trace-threads: 2
world-settings:
  default:
    anti-xray:
      ray-trace: true
      # About three times as resource intensive as false.
      ray-trace-third-person: true
      ray-trace-distance: 80.0
      rehide-blocks: true
      rehide-distance: 76.0
      section-leap: false
      max-ray-trace-block-count-per-chunk: 60
      ray-trace-blocks:
      - chest
      - diamond_ore
      - deepslate_diamond_ore
      - emerald_ore
      - deepslate_emerald_ore
      - gold_ore
      - deepslate_gold_ore
      - lapis_ore
      - deepslate_lapis_ore
      - mossy_cobblestone
      - spawner
  # Bukkit world name — adjust to match your server.
  world_nether:
    anti-xray:
      # Ancient debris never generates naturally exposed to air.
      # Paper engine-mode: 1 in the nether is enough; disable plugin ray tracing here.
      ray-trace: false
  world_the_end:
    anti-xray:
      ray-trace: false
```

---

## RayTraceAntiXray — optimized (lower CPU)

**Less CPU, weaker protection.** No third-person rays, no rehide, shorter distance, fewer blocks per chunk.

```yaml
settings:
  anti-xray:
    update-ticks: 1
    ms-per-ray-trace-tick: 50
    ray-trace-threads: 2
world-settings:
  default:
    anti-xray:
      ray-trace: true
      ray-trace-third-person: false
      ray-trace-distance: 64.0
      rehide-blocks: false
      rehide-distance: .inf
      section-leap: false
      max-ray-trace-block-count-per-chunk: 30
      ray-trace-blocks:
      - chest
      - diamond_ore
      - deepslate_diamond_ore
      - emerald_ore
      - deepslate_emerald_ore
      - gold_ore
      - deepslate_gold_ore
      - lapis_ore
      - deepslate_lapis_ore
      - spawner
  world_nether:
    anti-xray:
      ray-trace: false
  world_the_end:
    anti-xray:
      ray-trace: false
```

---

## Empty ray-trace-blocks list

If `ray-trace-blocks: []`, the plugin uses Paper's `hidden-blocks` from the Paper world config — the same list as the [Paper recommended overworld preset](#overworld-default) above. Convenient, but often heavier than an explicit list because every hidden block type is traced.

---

## Tuning with timings

```
/raytraceantixray timings on
/raytraceantixray timings off
```

Each tick logs ray-trace batch duration in **ms**. Aim for **under 50–100 ms**. Turn timings off afterward — it is very verbose.

---

## Further reading

- [Paper — Configuring Anti-Xray](https://docs.papermc.io/paper/anti-xray/) — official Paper presets (engine-mode 1 / 2 / 3)
- [Configuration reference](/docs/configuration) — every plugin key explained interactively
- [stonar96's settings gist](https://gist.github.com/stonar96/69ca0311392188b7ac2ece226286147f) — original author presets and demo configs
