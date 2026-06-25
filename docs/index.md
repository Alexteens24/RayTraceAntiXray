---
layout: home

hero:
  name: RayTraceAntiXray
  text: Hide Exposed Ores
  tagline: Server-side async ray tracing for Paper Anti-Xray engine-mode 1
  actions:
    - theme: brand
      text: Get Started
      link: /docs/
    - theme: alt
      text: Download
      link: /docs/download

features:
  - title: Engine-mode 1
    details: Extends Paper HIDE mode to obfuscate ores exposed to air — a gap vanilla Paper Anti-Xray does not cover.
    link: /docs/features
  - title: Async ray tracing
    details: Multithreaded line-of-sight tests run off the main thread and reveal only blocks the player can actually see.
    link: /docs/features
  - title: Folia ready
    details: Region-aware schedulers for block updates and packet handling. folia-supported in plugin.yml.
    link: /docs/installation
  - title: Universal JAR
    details: One build supports Paper 1.21.11 and 26.1.2 — no per-version classifier suffix.
    link: /docs/download
  - title: Runtime reload
    details: Reload config.yml with /raytraceantixray reload without a full restart for tuning changes.
    link: /docs/commands
  - title: Open source
    details: Fork of stonar96/RayTraceAntiXray. Source on GitHub; see license for JAR redistribution rules.
    link: https://github.com/Alexteens24/RayTraceAntiXray
---
