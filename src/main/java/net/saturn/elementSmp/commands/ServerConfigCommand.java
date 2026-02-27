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

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(ChatColor.GOLD + "=== Server Config ===");
            sender.sendMessage(ChatColor.YELLOW + "/" + label + " list");
            sender.sendMessage(ChatColor.YELLOW + "/" + label + " get <key>");
            sender.sendMessage(ChatColor.YELLOW + "/" + label + " set <key> <true|false>");
            sender.sendMessage(ChatColor.YELLOW + "/" + label + " reset [key]");
            sender.sendMessage(ChatColor.GRAY + "Keys:");
            sender.sendMessage(ChatColor.GRAY + "  abilities_enabled");
            sender.sendMessage(ChatColor.GRAY + "  element_roll_enabled");
            sender.sendMessage(ChatColor.GRAY + "  element_<air|water|fire|earth|life|death|metal|frost>");
            sender.sendMessage(ChatColor.GRAY + "  recipe_<upgrader1|upgrader2|reroller|advanced_reroller>");
            return true;
        }
        if (args.length < 2 && !args[0].equalsIgnoreCase("list") && !args[0].equalsIgnoreCase("reset")) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /" + label + " <help|list|get|set|reset> ...");
            return true;
        }

        String action = args[0].toLowerCase();
        String keyInput = args.length > 1 ? args[1] : "";
        String key = resolveKey(keyInput);
        switch (action) {
            case "list": {
                sender.sendMessage(ChatColor.GOLD + "=== Server Flags ===");
                sender.sendMessage(colorFlag("abilities_enabled", store.areAbilitiesEnabled()));
                sender.sendMessage(colorFlag("element_roll_enabled", store.isElementRollEnabled()));
                for (ElementType t : ElementType.values()) {
                    String el = t.name().toLowerCase();
                    sender.sendMessage(colorFlag("element_" + el, store.isElementEnabled(t)));
                }
                String[] recipes = {"upgrader1", "upgrader2", "reroller", "advanced_reroller"};
                for (String r : recipes) {
                    sender.sendMessage(colorFlag("recipe_" + r, store.isRecipeEnabled(r)));
                }
                return true;
            }
            case "get": {
                boolean val = store.getServerBoolean(key, false);
                sender.sendMessage(ChatColor.GREEN + keyInput + " = " + val);
                return true;
            }
            case "set": {
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.YELLOW + "Usage: /" + label + " set <key> <true|false>");
                    return true;
                }
                String valStr = args[2].toLowerCase();
                if (!valStr.equals("true") && !valStr.equals("false")) {
                    sender.sendMessage(ChatColor.RED + "Value must be true or false");
                    return true;
                }
                boolean val = Boolean.parseBoolean(valStr);
                store.setServerBoolean(key, val);
                sender.sendMessage(ChatColor.GREEN + "Set " + keyInput + " = " + val);
                
                if (key.startsWith("features.recipes.") && key.endsWith(".enabled")) {
                    String name = key.substring("features.recipes.".length(), key.length() - ".enabled".length());
                    applyRecipeToggle(name, val, sender);
                }
                return true;
            }
            case "reset": {
                if (args.length == 1) {
                    resetAll(sender);
                    return true;
                }
                String keyToResetInput = args[1];
                String keyToReset = resolveKey(keyToResetInput);
                store.setServerBoolean(keyToReset, true);
                sender.sendMessage(ChatColor.GREEN + "Reset " + keyToResetInput + " to default (true)");
                if (keyToReset.startsWith("features.recipes.") && keyToReset.endsWith(".enabled")) {
                    String name = keyToReset.substring("features.recipes.".length(), keyToReset.length() - ".enabled".length());
                    applyRecipeToggle(name, true, sender);
                }
                return true;
            }
            default:
                sender.sendMessage(ChatColor.YELLOW + "Usage: /" + label + " <help|list|get|set|reset> ...");
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("element.admin")) return new ArrayList<>();
        switch (args.length) {
            case 1:
                return filter(Arrays.asList("get", "set", "reset", "help", "list"), args[0]);
            case 2:
                List<String> keys = new ArrayList<>();
                keys.add("abilities");
                keys.add("roll");
                keys.add("abilities_enabled");
                keys.add("element_roll_enabled");
                keys.add("features.abilities_enabled");
                keys.add("features.element_roll_enabled");
                String[] recipes = {"upgrader1", "upgrader2", "reroller", "advanced_reroller"};
                for (String r : recipes) {
                    keys.add("recipes." + r);
                    keys.add("recipe_" + r);
                    keys.add("features.recipes." + r + ".enabled");
                }
                for (ElementType t : ElementType.values()) {
                    String el = t.name().toLowerCase();
                    keys.add("elements." + el);
                    keys.add("element_" + el);
                    keys.add("features.elements." + el + ".enabled");
                }
                return filter(keys, args[1]);
            case 3:
                if (args[0].equalsIgnoreCase("set")) {
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

    private void applyRecipeToggle(String name, boolean val, CommandSender sender) {
        switch (name) {
            case "upgrader1" -> {
                org.bukkit.NamespacedKey k = new org.bukkit.NamespacedKey(plugin, net.saturn.elementSmp.items.Upgrader1Item.KEY);
                if (val) net.saturn.elementSmp.items.Upgrader1Item.registerRecipe(plugin);
                else plugin.getServer().removeRecipe(k);
            }
            case "upgrader2" -> {
                org.bukkit.NamespacedKey k = new org.bukkit.NamespacedKey(plugin, net.saturn.elementSmp.items.Upgrader2Item.KEY);
                if (val) net.saturn.elementSmp.items.Upgrader2Item.registerRecipe(plugin);
                else plugin.getServer().removeRecipe(k);
            }
            case "reroller" -> {
                org.bukkit.NamespacedKey k = new org.bukkit.NamespacedKey(plugin, net.saturn.elementSmp.items.RerollerItem.KEY);
                if (val) net.saturn.elementSmp.items.RerollerItem.registerRecipe(plugin);
                else plugin.getServer().removeRecipe(k);
            }
            case "advanced_reroller" -> {
                org.bukkit.NamespacedKey k = new org.bukkit.NamespacedKey(plugin, net.saturn.elementSmp.items.AdvancedRerollerItem.KEY);
                if (val) net.saturn.elementSmp.items.AdvancedRerollerItem.registerRecipe(plugin);
                else plugin.getServer().removeRecipe(k);
            }
        }
    }

    private void resetAll(CommandSender sender) {
        store.setAbilitiesEnabled(true);
        store.setElementRollEnabled(true);
        for (ElementType type : ElementType.values()) {
            store.setElementEnabled(type, true);
        }
        String[] recipes = {"upgrader1", "upgrader2", "reroller", "advanced_reroller"};
        for (String r : recipes) {
            store.setRecipeEnabled(r, true);
            applyRecipeToggle(r, true, sender);
        }
        sender.sendMessage(ChatColor.GREEN + "All server flags reset to defaults.");
    }

    private String colorFlag(String name, boolean val) {
        return (val ? ChatColor.GREEN : ChatColor.RED) + name + " = " + val;
    }

    private String resolveKey(String input) {
        String s = input.toLowerCase();
        if (s.equals("abilities")) return "features.abilities_enabled";
        if (s.equals("abilities_enabled")) return "features.abilities_enabled";
        if (s.equals("roll")) return "features.element_roll_enabled";
        if (s.equals("element_roll_enabled")) return "features.element_roll_enabled";
        if (s.startsWith("element_")) {
            String el = s.substring("element_".length());
            return "features.elements." + el + ".enabled";
        }
        if (s.startsWith("elements.")) {
            String el = s.substring("elements.".length());
            return "features.elements." + el + ".enabled";
        }
        if (s.startsWith("recipes.")) {
            String r = s.substring("recipes.".length());
            return "features.recipes." + r + ".enabled";
        }
        if (s.startsWith("recipe_")) {
            String r = s.substring("recipe_".length());
            return "features.recipes." + r + ".enabled";
        }
        return input;
    }
}
