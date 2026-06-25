# Welcome to RayTraceAntiXray

**RayTraceAntiXray** is a Paper plugin that extends [Paper Anti-Xray](https://docs.papermc.io/paper/anti-xray/) **engine-mode 1** (`HIDE`). Paper hides buried ores by replacing them with decoy blocks in chunk packets, but ores **exposed to air** — on cave walls or in open pockets — remain visible. This plugin closes that gap with server-side async ray tracing.

## Quick Navigation

<CardGrid>

<DocCard icon="📥" title="Installation" link="/docs/installation" desc="Requirements, Paper Anti-Xray setup, PacketEvents, and first-run steps." />

<DocCard icon="📋" title="Recommended setup" link="/docs/recommended-configuration" desc="Copy-paste Paper Anti-Xray and plugin config presets for production." />

<DocCard icon="⚙️" title="Configuration" link="/docs/configuration" desc="Interactive reference for every config.yml key." />

<DocCard icon="⌨️" title="Commands" link="/docs/commands" desc="reload and timings subcommands with permission nodes." />

<DocCard icon="🔐" title="Permissions" link="/docs/permissions" desc="Full permission tree for command access." />

<DocCard icon="✨" title="Features" link="/docs/features" desc="How ray tracing works, pipeline stages, and limitations." />

<DocCard icon="🔧" title="Troubleshooting" link="/docs/troubleshooting" desc="Reload limits, Leaf compatibility, reconnect advice." />

</CardGrid>

## Why RayTraceAntiXray?

Paper Anti-Xray engine-mode 1 is effective for blocks not adjacent to air. RayTraceAntiXray adds a second layer: when a chunk is sent to a player, air-exposed ore positions are obfuscated immediately, then asynchronously ray-traced from the player's eye. Only blocks with an unobstructed line of sight are revealed via block-update packets.

For supported versions, block entities (e.g. chests) can be fully hidden — see Paper and plugin release notes.
