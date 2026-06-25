# Download

## Pre-built JAR

The primary distribution channel is [BuiltByBit — RayTraceAntiXray](https://builtbybit.com/resources/raytraceantixray.24914/).

Download the latest `.jar` and place it in your server's `plugins/` folder. See [Installation](/docs/installation) for full setup steps.

## Build from source

```bash
git clone https://github.com/Alexteens24/RayTraceAntiXray.git
cd RayTraceAntiXray
./gradlew build
```

The universal plugin JAR is produced at:

```
build/libs/RayTraceAntiXray-<version>.jar
```

One JAR supports **Paper 1.21.11** and **26.1.2** — no classifier suffix.

## Supported versions

| Minecraft / Paper | Server Java | Notes |
|-------------------|-------------|-------|
| **1.21.11** | 21+ | `api-version` floor in `plugin.yml` |
| **26.1.2** (26.x) | 25 recommended | Main NMS compiled against 26.1.2 dev bundle |

## License note

Source is governed by the [MIT license](https://github.com/Alexteens24/RayTraceAntiXray/blob/main/LICENSE). Redistribution of **compiled plugin JARs** intended for direct server use as RayTraceAntiXray is **not** permitted. Use as a library or shaded dependency for other projects is allowed.
