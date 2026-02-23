package net.saturn.elementSmp.commands;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.config.Constants;
import net.saturn.elementSmp.elements.ElementInfo;
import net.saturn.elementSmp.elements.ElementInfoRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.saturn.elementSmp.data.DataStore;
import net.saturn.elementSmp.elements.ElementType;
import net.saturn.elementSmp.gui.ElementSelectionGUI;
import net.saturn.elementSmp.managers.ElementManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class ElementCommand implements CommandExecutor, TabCompleter {
    private final ElementSmp plugin;
    private final DataStore dataStore;
    private final ElementManager elementManager;
    private final Map<String, SubCommand> subCommands;

    public ElementCommand(ElementSmp plugin) {
        this.plugin = plugin;
        this.dataStore = plugin.getDataStore();
        this.elementManager = plugin.getElementManager();
        this.subCommands = initializeSubCommands();
    }

    private Map<String, SubCommand> initializeSubCommands() {
        Map<String, SubCommand> commands = new HashMap<>();
        commands.put("set", new SetCommand());
        commands.put("debug", new DebugCommand());
        commands.put("roll", new RollCommand());
        commands.put("info", new InfoCommand());
        return commands;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(sub);
        if (subCommand != null) {
            // Allow '/element info' for all players; other subcommands require admin
            if (!sub.equals("info") && !sender.hasPermission("element.admin")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }
            return subCommand.execute(sender, args);
        }

        sendHelp(sender);
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Element Admin Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/element set <player> <element> - Set player's element");
        sender.sendMessage(ChatColor.YELLOW + "/element debug <player> - Debug player's element data");
        sender.sendMessage(ChatColor.YELLOW + "/element roll - Roll for a new element (OP only)");
        sender.sendMessage(ChatColor.YELLOW + "/element info <element> - View element details");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        boolean isAdmin = sender.hasPermission("element.admin");
        switch (args.length) {
            case 1:
                Set<String> base = new HashSet<>();
                base.add("info");
                if (isAdmin) {
                    base.addAll(subCommands.keySet());
                }
                return filterStartingWith(base, args[0]);
            case 2: {
                String subCmd = args[0].toLowerCase();
                if (subCmd.equals("info")) {
                    return filterStartingWith(getElementNames(), args[1]);
                }
                if (isAdmin && subCommands.containsKey(subCmd)) {
                    if (subCmd.equals("roll")) {
                        return Collections.emptyList();
                    }
                    return getOnlinePlayerNames(args[1]);
                }
                return Collections.emptyList();
            }
            case 3:
                if (isAdmin && args[0].equalsIgnoreCase("set")) {
                    return filterStartingWith(getElementNames(), args[2]);
                }
                return Collections.emptyList();
            default:
                return Collections.emptyList();
        }
    }

    private List<String> filterStartingWith(Collection<String> options, String prefix) {
        return options.stream()
                .filter(s -> s.toLowerCase().startsWith(prefix.toLowerCase()))
                .collect(Collectors.toList());
    }

    private List<String> getOnlinePlayerNames(String prefix) {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(prefix.toLowerCase()))
                .collect(Collectors.toList());
    }

    private List<String> getElementNames() {
        return Arrays.stream(ElementType.values())
                .map(type -> type.name().toLowerCase())
                .collect(Collectors.toList());
    }

    private interface SubCommand {
        boolean execute(CommandSender sender, String[] args);
    }

    private class SetCommand implements SubCommand {
        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (args.length < 3) {
                sender.sendMessage(ChatColor.RED + "Usage: /element set <player> <element>");
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player '" + args[1] + "' not found.");
                return true;
            }

            Optional<ElementType> elementType = parseElementType(args[2]);
            if (elementType.isEmpty()) {
                sender.sendMessage(ChatColor.RED + "Invalid element. Valid: " + String.join(", ", getElementNames()));
                return true;
            }

            plugin.getLogger().info(String.format("[ElementCommand] Setting element for %s (%s) to %s",
                    target.getName(), target.getUniqueId(), elementType.get().name()));

            elementManager.setElement(target, elementType.get());

            sender.sendMessage(ChatColor.GREEN + "Set " + target.getName() + "'s element to " +
                    ChatColor.AQUA + elementType.get().name());
            target.sendMessage(ChatColor.GREEN + "Your element has been set to " +
                    ChatColor.AQUA + elementType.get().name() + ChatColor.GREEN + " by an admin.");

            return true;
        }

        private Optional<ElementType> parseElementType(String input) {
            try {
                return Optional.of(ElementType.valueOf(input.toUpperCase()));
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        }
    }

    private class DebugCommand implements SubCommand {
        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Usage: /element debug <player>");
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player '" + args[1] + "' not found.");
                return true;
            }

            sender.sendMessage(ChatColor.GOLD + "=== Element Debug for " + target.getName() + " ===");

            ElementType managerElement = elementManager.getPlayerElement(target);
            sender.sendMessage(ChatColor.YELLOW + "ElementManager reports: " +
                    (managerElement != null ? managerElement.name() : "null"));

            dataStore.invalidateCache(target.getUniqueId());
            ElementType reloadedElement = elementManager.getPlayerElement(target);
            sender.sendMessage(ChatColor.YELLOW + "After cache invalidation: " +
                    (reloadedElement != null ? reloadedElement.name() : "null"));

            return true;
        }
    }

    private class RollCommand implements SubCommand {
        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                return true;
            }

            if (!player.isOp()) {
                sender.sendMessage(ChatColor.RED + "You must be OP to use this command.");
                return true;
            }

            if (elementManager.isCurrentlyRolling(player)) {
                player.sendMessage(ChatColor.RED + "You are already rolling for an element!");
                return true;
            }

            new ElementSelectionGUI(plugin, player, true).open();
            player.sendMessage(ChatColor.GREEN + "Rolling for a new element...");

            return true;
        }
    }

    private class InfoCommand implements SubCommand {
        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ChatColor.RED + "Only players can use this command!");
                return true;
            }
            if (args.length < 2) {
                player.sendMessage(Component.text("Usage: /element info <element>")
                        .color(NamedTextColor.YELLOW));
                return true;
            }
            showElementDetails(player, args[1]);
            return true;
        }

        private void showElementDetails(Player player, String elementName) {
            ElementType type;
            try {
                type = ElementType.valueOf(elementName.toUpperCase());
            } catch (IllegalArgumentException e) {
                player.sendMessage(Component.text("❌ Unknown element: " + elementName)
                        .color(NamedTextColor.RED));
                player.sendMessage(Component.text("Use /element info <element>")
                        .color(NamedTextColor.GRAY));
                return;
            }

            Optional<ElementInfo> infoOpt = ElementInfoRegistry.getInfo(type);
            if (infoOpt.isEmpty()) {
                player.sendMessage(Component.text("❌ No information available for " + type.name())
                        .color(NamedTextColor.RED));
                return;
            }

            ElementInfo info = infoOpt.get();
            TextColor elementColor = TextColor.color(info.color().asBungee().getColor().getRGB());

            player.sendMessage(Component.empty());
            player.sendMessage(Component.text("✦ ")
                    .color(NamedTextColor.GOLD)
                    .append(Component.text(type.name() + " ELEMENT")
                            .color(elementColor)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" ✦")
                            .color(NamedTextColor.GOLD)));
            player.sendMessage(Component.empty());

            player.sendMessage(Component.text("⭐ Passives")
                    .color(NamedTextColor.GOLD)
                    .decorate(TextDecoration.BOLD));
            for (String benefit : info.passiveBenefits()) {
                player.sendMessage(Component.text("  • ")
                        .color(NamedTextColor.DARK_GRAY)
                        .append(Component.text(benefit)
                                .color(NamedTextColor.GRAY)));
            }
            player.sendMessage(Component.empty());

            player.sendMessage(Component.text("⚡ Abilities")
                    .color(NamedTextColor.GOLD)
                    .decorate(TextDecoration.BOLD));

            displayAbility(player, info.ability1(), 1, NamedTextColor.AQUA);
            displayAbility(player, info.ability2(), 2, NamedTextColor.LIGHT_PURPLE);

            player.sendMessage(Component.empty());
        }

        private void displayAbility(Player player, ElementInfo.AbilityInfo ability, int index, TextColor nameColor) {
            String numIcon = index == 1 ? "①" : "②";
            String upgradeTag = ability.requiredUpgradeLevel() == 1 ? "Upgrade I" : "Upgrade II";

            player.sendMessage(Component.text("  " + numIcon + " ")
                    .color(nameColor)
                    .append(Component.text(ability.name())
                            .color(nameColor)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" [" + upgradeTag + "]")
                            .color(NamedTextColor.DARK_GRAY)
                            .decorate(TextDecoration.ITALIC)));

            player.sendMessage(Component.text("     " + ability.description())
                    .color(NamedTextColor.GRAY));

            player.sendMessage(Component.text("     " + Constants.Mana.ICON + " Mana: ")
                    .color(NamedTextColor.DARK_AQUA)
                    .append(Component.text(ability.manaCost())
                            .color(NamedTextColor.AQUA)));
            player.sendMessage(Component.empty());
        }
    }
}
