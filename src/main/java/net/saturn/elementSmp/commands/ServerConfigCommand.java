package net.saturn.elementSmp.commands;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.data.DataStore;
import net.saturn.elementSmp.elements.ElementType;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ServerConfigCommand implements CommandExecutor, TabCompleter {
    private final ElementSmp plugin;
    private final DataStore store;

    public ServerConfigCommand(ElementSmp plugin) {
        this.plugin = plugin;
        this.store = plugin.getDataStore();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("element.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.GOLD + "=== Server Config ===");
            sender.sendMessage(ChatColor.YELLOW + "/" + label + " element <air|water|fire|earth|life|death|metal|frost> <true|false>");
            sender.sendMessage(ChatColor.YELLOW + "/" + label + " abilities <true|false>");
            sender.sendMessage(ChatColor.YELLOW + "/" + label + " upgrader1 <true|false>");
            sender.sendMessage(ChatColor.YELLOW + "/" + label + " upgrader2 <true|false>");
            sender.sendMessage(ChatColor.YELLOW + "/" + label + " reroller <true|false>");
            sender.sendMessage(ChatColor.YELLOW + "/" + label + " advanced_reroller <true|false>");
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "element": {
                if (args.length != 3) {
                    sender.sendMessage(ChatColor.YELLOW + "Usage: /" + label + " element <air|water|fire|earth|life|death|metal|frost> <true|false>");
                    return true;
                }

                String elementName = args[1].toUpperCase();
                ElementType type;
                try {
                    type = ElementType.valueOf(elementName);
                } catch (IllegalArgumentException ex) {
                    sender.sendMessage(ChatColor.RED + "Unknown element: " + args[1]);
                    return true;
                }

                Boolean val = parseBooleanArg(args[2], sender);
                if (val == null) return true;

                store.setElementEnabled(type, val);
                sender.sendMessage(ChatColor.GREEN + "element_" + type.name().toLowerCase() + " set to " + val);
                return true;
            }
            case "abilities": {
                if (args.length != 2) {
                    sender.sendMessage(ChatColor.YELLOW + "Usage: /" + label + " abilities <true|false>");
                    return true;
                }
                Boolean val = parseBooleanArg(args[1], sender);
                if (val == null) return true;
                store.setAbilitiesEnabled(val);
                sender.sendMessage(ChatColor.GREEN + "abilities_enabled set to " + val);
                return true;
            }
            case "upgrader1":
            case "upgrader2":
            case "reroller":
            case "advanced_reroller": {
                if (args.length != 2) {
                    sender.sendMessage(ChatColor.YELLOW + "Usage: /" + label + " " + sub + " <true|false>");
                    return true;
                }
                Boolean val = parseBooleanArg(args[1], sender);
                if (val == null) return true;
                handleRecipeToggle(sub, val, sender);
                return true;
            }
            default:
                sender.sendMessage(ChatColor.YELLOW + "Unknown subcommand. Use /" + label + " for help.");
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("element.admin")) return new ArrayList<>();
        switch (args.length) {
            case 1:
                return filter(Arrays.asList("element", "abilities", "upgrader1", "upgrader2", "reroller", "advanced_reroller"), args[0]);
            case 2:
                if (args[0].equalsIgnoreCase("element")) {
                    List<String> elements = new ArrayList<>();
                    for (ElementType t : ElementType.values()) {
                        elements.add(t.name().toLowerCase());
                    }
                    return filter(elements, args[1]);
                } else if (args[0].equalsIgnoreCase("abilities")
                        || args[0].equalsIgnoreCase("upgrader1")
                        || args[0].equalsIgnoreCase("upgrader2")
                        || args[0].equalsIgnoreCase("reroller")
                        || args[0].equalsIgnoreCase("advanced_reroller")) {
                    return filter(Arrays.asList("true", "false"), args[1]);
                }
                return new ArrayList<>();
            case 3:
                if (args[0].equalsIgnoreCase("element")) {
                    return filter(Arrays.asList("true", "false"), args[2]);
                }
                return new ArrayList<>();
            default:
                return new ArrayList<>();
        }
    }

    private List<String> filter(List<String> options, String prefix) {
        return options.stream()
                .filter(s -> s.toLowerCase().startsWith(prefix.toLowerCase()))
                .collect(Collectors.toList());
    }

    private Boolean parseBooleanArg(String raw, CommandSender sender) {
        String valStr = raw.toLowerCase();
        if (!valStr.equals("true") && !valStr.equals("false")) {
            sender.sendMessage(ChatColor.RED + "Value must be true or false");
            return null;
        }
        return Boolean.parseBoolean(valStr);
    }

    private void handleRecipeToggle(String name, boolean enabled, CommandSender sender) {
        store.setRecipeEnabled(name, enabled);

        switch (name) {
            case "upgrader1" -> {
                org.bukkit.NamespacedKey k = new org.bukkit.NamespacedKey(plugin, net.saturn.elementSmp.items.Upgrader1Item.KEY);
                if (enabled) net.saturn.elementSmp.items.Upgrader1Item.registerRecipe(plugin);
                else plugin.getServer().removeRecipe(k);
            }
            case "upgrader2" -> {
                org.bukkit.NamespacedKey k = new org.bukkit.NamespacedKey(plugin, net.saturn.elementSmp.items.Upgrader2Item.KEY);
                if (enabled) net.saturn.elementSmp.items.Upgrader2Item.registerRecipe(plugin);
                else plugin.getServer().removeRecipe(k);
            }
            case "reroller" -> {
                org.bukkit.NamespacedKey k = new org.bukkit.NamespacedKey(plugin, net.saturn.elementSmp.items.RerollerItem.KEY);
                if (enabled) net.saturn.elementSmp.items.RerollerItem.registerRecipe(plugin);
                else plugin.getServer().removeRecipe(k);
            }
            case "advanced_reroller" -> {
                org.bukkit.NamespacedKey k = new org.bukkit.NamespacedKey(plugin, net.saturn.elementSmp.items.AdvancedRerollerItem.KEY);
                if (enabled) net.saturn.elementSmp.items.AdvancedRerollerItem.registerRecipe(plugin);
                else plugin.getServer().removeRecipe(k);
            }
        }

        sender.sendMessage(ChatColor.GREEN + "recipe_" + name + " set to " + enabled);
    }
}
