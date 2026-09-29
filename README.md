# RayTraceAntiXray

Paper Anti-Xray engine-mode 1 can't hide ores exposed in caves. This plugin ray-traces from each player's eyes and only hides blocks that are genuinely in the open.

**Paper 26.3 · 26.2 · 26.1.2 · Folia** — one universal JAR.

[🇺🇸 English](README.md) · [🇻🇳 Tiếng Việt](README_vn.md)

## Requirements

- Paper (or a Folia-compatible fork)
- Paper Anti-Xray with `engine-mode: 1` — [docs](https://docs.papermc.io/paper/anti-xray/)
- [PacketEvents](https://modrinth.com/plugin/packetevents) (`depend` — the plugin won't load without it)

## Install

1. Download the JAR from [Modrinth](https://modrinth.com/plugin/forkedraytraceantixray)
2. Drop it into `plugins/`
3. Set `engine-mode: 1` in `config/paper-world-defaults.yml`
4. Restart the server

> Don't use `/reload` or hot-swap the JAR. Replacing the JAR or changing Paper Anti-Xray requires a full restart.

## Configuration

`plugins/RayTraceAntiXray/config.yml` — per world, or start from the [Recommended setup](https://alexteens24.github.io/RayTraceAntiXray/docs/recommended-configuration) presets. Full reference: [Configuration](https://alexteens24.github.io/RayTraceAntiXray/docs/configuration).

## Commands

Requires `raytraceantixray.command.raytraceantixray`; each subcommand has its own permission — see [Permissions](https://alexteens24.github.io/RayTraceAntiXray/docs/permissions).

| Command | Description |
|---------|-------------|
| `/raytraceantixray reload` | Reload `config.yml` |
| `/raytraceantixray timings <on\|off>` | Toggle the timing report |
| `/raytraceantixray reminder [dismiss\|enable]` | Show operators the Anti-Xray setup status |

## Build

```bash
./gradlew build     # -> build/libs/RayTraceAntiXray-<version>.jar
./gradlew test      # unit tests
./gradlew run26_3   # test server (also run1_21_11 / run26_1_2 / run26_2)
```

Multi-NMS architecture details: [FORK.md](FORK.md).

## Documentation

[alexteens24.github.io/RayTraceAntiXray](https://alexteens24.github.io/RayTraceAntiXray/)

## Credits

Original: [stonar96/RayTraceAntiXray](https://github.com/stonar96/RayTraceAntiXray) · Dirty-tracking idea: [TauCu/RayTraceAntiXray](https://github.com/TauCu/RayTraceAntiXray)

MIT — see [LICENSE](LICENSE).
