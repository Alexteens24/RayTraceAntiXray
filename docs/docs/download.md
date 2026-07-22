# Download

Download the latest release from **[Modrinth — RayTraceAntiXray (fork)](https://modrinth.com/plugin/forkedraytraceantixray)**.

Place the `.jar` in your server's `plugins/` folder and follow the [Installation](/docs/installation) guide.

## Supported versions

| Minecraft / Paper | Server Java |
|-------------------|-------------|
| **1.21.11** | 21+ |
| **26.1.2** (26.x) | 25 recommended |

One universal JAR covers all supported versions — no classifier suffix.

## Building from source

For contributors only — server operators should use Modrinth.

```bash
git clone https://github.com/Alexteens24/RayTraceAntiXray.git
cd RayTraceAntiXray
./gradlew build
```

Output: `build/libs/RayTraceAntiXray-<version>.jar`. See [Development](/docs/development) for tests and local servers.

## Credits

Original plugin by **stonar96** — [stonar96/RayTraceAntiXray](https://github.com/stonar96/RayTraceAntiXray).

Dirty-tracking and supporting data-structure design were inspired by **TauCu's fork** — [TauCu/RayTraceAntiXray](https://github.com/TauCu/RayTraceAntiXray).

Licensed [MIT](https://github.com/Alexteens24/RayTraceAntiXray/blob/main/LICENSE).
