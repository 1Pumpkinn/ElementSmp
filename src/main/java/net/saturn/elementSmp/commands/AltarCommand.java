package net.saturn.elementSmp.commands;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.altar.AltarManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AltarCommand implements CommandExecutor, TabCompleter {
    private final ElementSmp plugin;
    private final AltarManager altarManager;

    public AltarCommand(ElementSmp plugin, AltarManager altarManager) {
        this.plugin = plugin;
        this.altarManager = altarManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        if (!player.hasPermission("element.admin")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length < 2 || !args[0].equalsIgnoreCase("place")) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /altar place <recipe_name>");
            player.sendMessage(ChatColor.YELLOW + "Available recipes: " + String.join(", ", altarManager.getRecipeNames()));
            return true;
        }

        String recipeName = args[1].toLowerCase();
        if (altarManager.placeAltar(player.getLocation().getBlock().getLocation(), recipeName)) {
            player.sendMessage(ChatColor.GREEN + "Altar for " + recipeName + " placed successfully!");
        } else {
            player.sendMessage(ChatColor.RED + "Invalid recipe name: " + recipeName);
            player.sendMessage(ChatColor.RED + "Available recipes: " + String.join(", ", altarManager.getRecipeNames()));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("element.admin")) return new ArrayList<>();

        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            list.add("place");
            return list.stream().filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("place")) {
            return altarManager.getRecipeNames().stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}
