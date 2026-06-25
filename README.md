# RayTraceAntiXray (fork)

Fork of [stonar96/RayTraceAntiXray](https://github.com/stonar96/RayTraceAntiXray). Extends [Paper Anti-Xray](https://docs.papermc.io/paper/anti-xray/) **engine-mode 1** with server-side async ray tracing so ores **exposed to air** can be obfuscated and revealed only when the player has line of sight.

**Download:** https://modrinth.com/plugin/forkedraytraceantixray  
**Documentation:** https://alexteens24.github.io/RayTraceAntiXray/

![Demonstration](https://user-images.githubusercontent.com/18699205/185815590-4b2efce6-5a26-4579-b079-e9958a454fd0.gif)

## Requirements

| | |
|---|---|
| **Server** | Paper or Folia — **1.21.11** or **26.1.2** |
| **Paper Anti-Xray** | `engine-mode: 1` |
| **PacketEvents** | [Modrinth](https://modrinth.com/plugin/packetevents) (required dependency) |

## Quick install

1. Enable Paper Anti-Xray with `engine-mode: 1` and restart.
2. Install **PacketEvents**.
3. Download the JAR from [Modrinth](https://modrinth.com/plugin/forkedraytraceantixray) into `plugins/`.
4. Edit `plugins/RayTraceAntiXray/config.yml`.
5. **Restart** the server (do not use Bukkit `/reload`).

Full setup, configuration, commands, and troubleshooting: **[documentation site](https://alexteens24.github.io/RayTraceAntiXray/)**.

Recommended tuning: [docs — Recommended setup](https://alexteens24.github.io/RayTraceAntiXray/docs/recommended-configuration).

## Development

```bash
./gradlew build    # → build/libs/RayTraceAntiXray-<version>.jar
./gradlew test
./gradlew run1_21_11
./gradlew run26_1_2
```

Fork-specific notes (multi-NMS, PacketEvents, Folia, Leaf): [FORK.md](FORK.md).

## License

[MIT](LICENSE). Original plugin by **stonar96**.
