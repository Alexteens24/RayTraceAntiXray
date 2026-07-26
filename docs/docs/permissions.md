# Permissions

Sub-permissions are checked in code. Grant them explicitly in your permissions plugin — they are not declared as children in `plugin.yml`. Server operators bypass permission checks by default.

<BaseTable :columns="['Permission', 'Description', 'Default']" grid="2fr 3fr 0.6fr">

<PermRow permission="raytraceantixray.command.raytraceantixray" defaultVal="op">
Base command permission (Bukkit-enforced via <code>plugin.yml</code>). Required before any subcommand runs.
</PermRow>

<PermRow permission="raytraceantixray.command.raytraceantixray.reload" defaultVal="none">
Use <code>/raytraceantixray reload</code>.
</PermRow>

<PermRow permission="raytraceantixray.reminder" defaultVal="op">
Receive Paper Anti-Xray setup reminders and use <code>/raytraceantixray reminder [dismiss|enable]</code>. Dismiss and enable affect the whole server.
</PermRow>

<PermRow permission="raytraceantixray.command.raytraceantixray.timings" defaultVal="none">
Parent for timings subcommands; checked before <code>on</code> or <code>off</code> is accepted.
</PermRow>

<PermRow permission="raytraceantixray.command.raytraceantixray.timings.on" defaultVal="none">
Use <code>/raytraceantixray timings on</code>. Requires <code>.timings</code> as well.
</PermRow>

<PermRow permission="raytraceantixray.command.raytraceantixray.timings.off" defaultVal="none">
Use <code>/raytraceantixray timings off</code>. Requires <code>.timings</code> as well.
</PermRow>

</BaseTable>

::: tip In-game reference
On first run the plugin writes <code>plugins/RayTraceAntiXray/README.txt</code> with the same permission tree for quick lookup on the server.
:::
