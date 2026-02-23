package net.saturn.elementSmp.commands;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.config.Constants;
import net.saturn.elementSmp.elements.ElementInfo;
import net.saturn.elementSmp.elements.ElementInfoRegistry;
import net.saturn.elementSmp.elements.ElementType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class ElementInfoCommand implements CommandExecutor, TabCompleter {

    public ElementInfoCommand(ElementSmp plugin) {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(Component.text("Usage: /" + label + " <element>")
                    .color(NamedTextColor.YELLOW));
            return true;
        }

        showElementDetails(player, label, args[0]);
        return true;
    }

    /**
     * Show detailed information about a specific element
     */
    private void showElementDetails(Player player, String label, String elementName) {
        ElementType type;
        try {
            type = ElementType.valueOf(elementName.toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(Component.text("❌ Unknown element: " + elementName)
                    .color(NamedTextColor.RED));
            player.sendMessage(Component.text("Use /" + label + " <element>")
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

        // Header
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

        // Abilities
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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            String input = args[0].toLowerCase();

            for (ElementType type : ElementType.values()) {
                String name = type.name().toLowerCase();
                if (name.startsWith(input)) {
                    completions.add(name);
                }
            }
            return completions;
        }
        return Collections.emptyList();
    }
}
