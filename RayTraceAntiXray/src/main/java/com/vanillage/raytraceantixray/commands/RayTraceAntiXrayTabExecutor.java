package com.vanillage.raytraceantixray.commands;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import com.vanillage.raytraceantixray.RayTraceAntiXrayCommandTarget;
import com.vanillage.raytraceantixray.reminder.PaperAntiXrayReminder;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class RayTraceAntiXrayTabExecutor implements TabExecutor {
    private final RayTraceAntiXrayCommandTarget plugin;

    public RayTraceAntiXrayTabExecutor(RayTraceAntiXrayCommandTarget plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        LinkedList<String> completions = new LinkedList<>();

        if (args.length == 0) {
            if ("raytraceantixray".startsWith(label.toLowerCase(Locale.ROOT))) {
                completions.add("raytraceantixray");
            }
        } else if (command.getName().toLowerCase(Locale.ROOT).equals("raytraceantixray")) {
            if (args.length == 1) {
                if (sender.hasPermission("raytraceantixray.command.raytraceantixray.timings") && "timings".startsWith(args[0].toLowerCase(Locale.ROOT))) {
                    completions.add("timings");
                }

                if (sender.hasPermission("raytraceantixray.command.raytraceantixray.reload") && "reload".startsWith(args[0].toLowerCase(Locale.ROOT))) {
                    completions.add("reload");
                }

                if (sender.hasPermission(PaperAntiXrayReminder.PERMISSION) && "reminder".startsWith(args[0].toLowerCase(Locale.ROOT))) {
                    completions.add("reminder");
                }
            } else if (args[0].toLowerCase(Locale.ROOT).equals("timings")) {
                if (sender.hasPermission("raytraceantixray.command.raytraceantixray.timings")) {
                    if (args.length == 2) {
                        if (sender.hasPermission("raytraceantixray.command.raytraceantixray.timings.on") && "on".startsWith(args[1].toLowerCase(Locale.ROOT))) {
                            completions.add("on");
                        }

                        if (sender.hasPermission("raytraceantixray.command.raytraceantixray.timings.off") && "off".startsWith(args[1].toLowerCase(Locale.ROOT))) {
                            completions.add("off");
                        }
                    }
                }
            } else if (args[0].toLowerCase(Locale.ROOT).equals("reload")) {

            } else if (args[0].toLowerCase(Locale.ROOT).equals("reminder")) {
                if (sender.hasPermission(PaperAntiXrayReminder.PERMISSION) && args.length == 2) {
                    if ("dismiss".startsWith(args[1].toLowerCase(Locale.ROOT))) {
                        completions.add("dismiss");
                    }

                    if ("enable".startsWith(args[1].toLowerCase(Locale.ROOT))) {
                        completions.add("enable");
                    }
                }
            }
        }

        return completions;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().toLowerCase(Locale.ROOT).equals("raytraceantixray")) {
            if (args.length == 0) {

            } else if (args[0].toLowerCase(Locale.ROOT).equals("reload")) {
                if (sender.hasPermission("raytraceantixray.command.raytraceantixray.reload")) {
                    if (args.length == 1) {
                        plugin.reloadPluginConfiguration();
                        sender.sendMessage(Component.text("RayTraceAntiXray configuration reloaded."));
                        return true;
                    }
                } else {
                    sender.sendMessage(Component.text("You don't have permissions.", NamedTextColor.RED));
                    return true;
                }
            } else if (args[0].toLowerCase(Locale.ROOT).equals("timings")) {
                if (sender.hasPermission("raytraceantixray.command.raytraceantixray.timings")) {
                    if (args.length == 1) {

                    } else if (args[1].toLowerCase(Locale.ROOT).equals("on")) {
                        if (sender.hasPermission("raytraceantixray.command.raytraceantixray.timings.on")) {
                            if (args.length == 2) {
                                plugin.setTimingsEnabled(true);
                                sender.sendMessage(Component.text("Timings turned on."));
                                return true;
                            }
                        } else {
                            sender.sendMessage(Component.text("You don't have permissions.", NamedTextColor.RED));
                            return true;
                        }
                    } else if (args[1].toLowerCase(Locale.ROOT).equals("off")) {
                        if (sender.hasPermission("raytraceantixray.command.raytraceantixray.timings.off")) {
                            if (args.length == 2) {
                                plugin.setTimingsEnabled(false);
                                sender.sendMessage(Component.text("Timings turned off."));
                                return true;
                            }
                        } else {
                            sender.sendMessage(Component.text("You don't have permissions.", NamedTextColor.RED));
                            return true;
                        }
                    }
                } else {
                    sender.sendMessage(Component.text("You don't have permissions.", NamedTextColor.RED));
                    return true;
                }
            } else if (args[0].toLowerCase(Locale.ROOT).equals("reminder")) {
                if (!sender.hasPermission(PaperAntiXrayReminder.PERMISSION)) {
                    sender.sendMessage(Component.text("You don't have permissions.", NamedTextColor.RED));
                    return true;
                }

                if (args.length == 1) {
                    sendReminderStatus(sender);
                    return true;
                }

                if (args.length == 2 && args[1].toLowerCase(Locale.ROOT).equals("dismiss")) {
                    if (plugin.setPaperAntiXrayReminderEnabled(false)) {
                        sender.sendMessage(Component.text("Got it — setup reminders are now off for this server.", NamedTextColor.GREEN));
                    } else {
                        sender.sendMessage(Component.text("Could not save the reminder setting. Check the server log.", NamedTextColor.RED));
                    }
                    return true;
                }

                if (args.length == 2 && args[1].toLowerCase(Locale.ROOT).equals("enable")) {
                    if (plugin.setPaperAntiXrayReminderEnabled(true)) {
                        sender.sendMessage(Component.text("Setup reminders are back on.", NamedTextColor.GREEN));
                        sendReminderStatus(sender);
                    } else {
                        sender.sendMessage(Component.text("Could not save the reminder setting. Check the server log.", NamedTextColor.RED));
                    }
                    return true;
                }
            }
        }

        return false;
    }

    private void sendReminderStatus(CommandSender sender) {
        if (!plugin.isPaperAntiXrayReminderEnabled()) {
            sender.sendMessage(Component.text("Setup reminders are currently off. Use /raytraceantixray reminder enable whenever you want them back.", NamedTextColor.GRAY));
            return;
        }

        List<String> incompatibleWorlds = plugin.getIncompatiblePaperAntiXrayWorlds();

        if (incompatibleWorlds.isEmpty()) {
            sender.sendMessage(Component.text("Everything looks good! Paper Anti-Xray is ready for all enabled RayTraceAntiXray worlds.", NamedTextColor.GREEN));
        } else {
            sender.sendMessage(PaperAntiXrayReminder.message(incompatibleWorlds));
        }
    }
}
