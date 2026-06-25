# Configuration

The `config.yml` file lives in `plugins/RayTraceAntiXray/`. Global scheduler settings are under `settings.anti-xray`; per-world overrides live under `world-settings.<world>.anti-xray` (all worlds inherit from `default`).

**New to the plugin?** Start with [Recommended setup](/docs/recommended-configuration) — copy-paste Paper Anti-Xray + plugin presets for production.

Click any option or category below to view additional information.

::: tip Apply changes without a restart
After editing `config.yml`, run <code>/raytraceantixray reload</code> to apply changes. A full restart is still required for JAR or dependency updates.
:::

<div style="background-color: var(--vp-c-bg-alt); padding: 20px; border-radius: 12px; margin-top: 20px;">

<ConfigGroup name="settings">
<template #info>
Global ray-trace scheduler settings shared across all worlds.
</template>

<ConfigGroup name="anti-xray">

<ConfigProperty name="update-ticks" :value="1" type="number">
Time period in Minecraft ticks for sending block updates to reveal blocks to the player.
</ConfigProperty>

<ConfigProperty name="ms-per-ray-trace-tick" :value="50" type="number">
Target time per ray-trace tick in milliseconds. Controls how often the async scheduler runs a batch of ray traces.
</ConfigProperty>

<ConfigProperty name="ray-trace-threads" :value="2" type="number">
Number of threads in the fixed pool used for visibility work. Higher values increase CPU use.
</ConfigProperty>

</ConfigGroup>
</ConfigGroup>

<ConfigGroup name="world-settings">
<template #info>
Per-world overrides. Worlds not listed inherit all settings from the <code>default</code> section.
</template>

<ConfigGroup name="default">

<ConfigGroup name="anti-xray">
<template #info>
Ray tracing and block-selection settings for this world. Paper Anti-Xray must also be enabled with <code>engine-mode: 1</code>.
</template>

<ConfigProperty name="ray-trace" :value="true" type="boolean">
Whether ray tracing is enabled for this world. Requires Paper Anti-Xray with engine-mode 1.
</ConfigProperty>

<ConfigProperty name="ray-trace-third-person" :value="false" type="boolean">
Whether to ray trace from third-person camera origins (back and front). Significantly more resource intensive.
</ConfigProperty>

<ConfigProperty name="ray-trace-distance" :value="80.0" type="number">
Maximum distance in blocks between block center and player eye for visibility tests. Blocks beyond this distance keep their previous hidden or revealed state.
</ConfigProperty>

<ConfigProperty name="rehide-blocks" :value="false" type="boolean">
When true, blocks the player can no longer see are obfuscated again without waiting for a full chunk resend.
</ConfigProperty>

<ConfigProperty name="rehide-distance" value=".inf" type="string">
Distance threshold for rehide logic. Blocks at or beyond this distance from the player eye are treated as invisible and rehidden (when <code>rehide-blocks</code> is enabled). Use <code>.inf</code> to disable the rehide distance cap.
</ConfigProperty>

<ConfigProperty name="section-leap" :value="false" type="boolean">
When true, skip voxel steps across 16³ chunk sections that report air-only (<code>hasOnlyAir</code>). Off by default; enable only after testing on your hardware.
</ConfigProperty>

<ConfigProperty name="max-ray-trace-block-count-per-chunk" :value="100" type="number">
Maximum number of initially hidden and ray-traced block positions per chunk. Counting starts at the bottom of the world and increases upward.
</ConfigProperty>

<ConfigProperty name="ray-trace-blocks" :value="[]" type="list">
Block types to hide and ray trace. If empty, Paper's <code>hidden-blocks</code> list from the Paper config is used. Paper settings such as <code>max-block-height</code> also apply.
</ConfigProperty>

</ConfigGroup>
</ConfigGroup>
</ConfigGroup>

</div>

## Per-world example

Disable ray tracing in a lobby world while keeping it enabled elsewhere:

```yaml
world-settings:
  default:
    anti-xray:
      ray-trace: true
  lobby:
    anti-xray:
      ray-trace: false
```

## Full example

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
      ray-trace-distance: 80.0
      rehide-blocks: false
      rehide-distance: .inf
      section-leap: false
      max-ray-trace-block-count-per-chunk: 100
      ray-trace-blocks: []
```

See the commented example in the plugin source for additional block-list presets (diamond ore, emerald ore, etc.).
