package net.saturn.elementsmp.managers;

import net.saturn.elementsmp.ElementSmp;
import net.saturn.elementsmp.commands.*;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.logging.Level;

public class CommandManager {
    private final ElementSmp plugin;

    public CommandManager(ElementSmp plugin) {
        this.plugin = plugin;
    }

    public void registerCommands() {
        plugin.getLogger().info("Registering commands...");

        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                var commandMap = Bukkit.getCommandMap();

                registerCommand(commandMap, "elements", new ElementInfoCommand(plugin), "View element info");
                registerCommand(commandMap, "trust", new TrustCommand(plugin, plugin.getTrustManager()), "Manage trust list");
                registerCommand(commandMap, "element", new ElementCommand(plugin), "Admin element commands");
                
                var manaCmd = registerCommand(commandMap, "mana", new ManaCommand(plugin.getManaManager(), plugin.getConfigManager()), "Manage mana");
                manaCmd.setPermission("element.admin");
                manaCmd.setPermissionMessage("§cYou don't have permission to use this command.");

                var utilCmd = registerCommand(commandMap, "util", new UtilCommand(plugin), "Utility commands");
                utilCmd.setPermission("element.admin");
                utilCmd.setPermissionMessage("§cYou don't have permission to use this command.");

                registerAbilityCommand(commandMap, "ability1", 1, "Use Ability 1");
                registerAbilityCommand(commandMap, "ability2", 2, "Use Ability 2");

                var serverCfgCmd = registerCommand(commandMap, "servercfg", new ServerConfigCommand(plugin), "Manage server flags");
                serverCfgCmd.setPermission("element.admin");
                serverCfgCmd.setPermissionMessage("§cYou don't have permission to use this command.");

                var altarCmd = registerCommand(commandMap, "altar", new AltarCommand(plugin, plugin.getAltarManager()), "Altar management commands");
                altarCmd.setPermission("element.admin");
                altarCmd.setPermissionMessage("§cYou don't have permission to use this command.");

                plugin.getLogger().info("Commands registered successfully");
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to register commands", e);
            }
        });
    }

    private BukkitCommand registerCommand(org.bukkit.command.CommandMap commandMap, String name, org.bukkit.command.CommandExecutor executor, String description) {
        var command = new BukkitCommand(name) {
            @Override
            public boolean execute(CommandSender sender, String label, String[] args) {
                return executor.onCommand(sender, this, label, args);
            }

            @Override
            public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
                if (executor instanceof org.bukkit.command.TabCompleter tabCompleter) {
                    return tabCompleter.onTabComplete(sender, this, alias, args);
                }
                return super.tabComplete(sender, alias, args);
            }
        };
        command.setDescription(description);
        commandMap.register("elementsmp", command);
        return command;
    }

    private void registerAbilityCommand(org.bukkit.command.CommandMap commandMap, String name, int abilityNumber, String description) {
        var command = new BukkitCommand(name) {
            @Override
            public boolean execute(CommandSender sender, String label, String[] args) {
                if (!(sender instanceof Player player)) return false;
                if (!plugin.getDataStore().areAbilitiesEnabled()) {
                    player.sendMessage("§cAbilities are currently disabled by the server.");
                    return true;
                }
                return plugin.getAbilityListener().triggerAbility(player, abilityNumber);
            }
        };
        command.setDescription(description);
        commandMap.register("elementsmp", command);
    }
}
