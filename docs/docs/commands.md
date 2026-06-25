# Commands

All commands are under `/raytraceantixray`. Tab-completion suggests subcommands you have permission for.

<div class="command-section">

<CommandRow commands="/raytraceantixray reload" permission="raytraceantixray.command.raytraceantixray.reload">
Reloads <code>config.yml</code> and reapplies ray-trace scheduling, world controllers, and online player tasks.
<ul>
<li>Does <strong>not</strong> replace a restart after JAR, PacketEvents, or Paper Anti-Xray changes.</li>
<li>Chunk block lists rebuild only when chunks are resent — have players reconnect if obfuscation looks wrong.</li>
</ul>
</CommandRow>

<CommandRow commands="/raytraceantixray timings on" permission="raytraceantixray.command.raytraceantixray.timings.on">
Enables per-tick ray batch duration logging to the console.
Requires <code>raytraceantixray.command.raytraceantixray.timings</code> as well.
</CommandRow>

<CommandRow commands="/raytraceantixray timings off" permission="raytraceantixray.command.raytraceantixray.timings.off">
Disables timings output. Also requires the <code>.timings</code> parent node.
</CommandRow>

</div>

::: tip Base permission
Bukkit checks <code>raytraceantixray.command.raytraceantixray</code> (declared on the command in <code>plugin.yml</code>) before any subcommand runs.
:::

## What reload does

`reloadPluginConfiguration()` runs on the main thread and:

1. Reloads `config.yml` from disk.
2. Shuts down and recreates the ray-trace thread pool and async tick.
3. Reapplies `ChunkPacketBlockControllerAntiXray` on all loaded worlds.
4. Clears and re-registers all online players.

See [Troubleshooting](/docs/troubleshooting) for cases where a full restart is still required.
