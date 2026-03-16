package net.saturn.elementsmp.listeners.item.core;

import net.saturn.elementsmp.ElementSmp;
import net.saturn.elementsmp.items.ItemKeys;
import org.bukkit.ChatColor;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class ItemGlowListener implements Listener {
    private final ElementSmp plugin;

    public ItemGlowListener(ElementSmp plugin) {
        this.plugin = plugin;
        // Try to create teams, but don't crash if scoreboard manager isn't ready yet
        try {
            createTeams();
        } catch (Exception ignored) {}
    }

    private void createTeams() {
        var manager = plugin.getServer().getScoreboardManager();
        if (manager == null) return;
        
        Scoreboard scoreboard = manager.getMainScoreboard();
        if (scoreboard.getTeam("altar_glow") == null) {
            Team altarTeam = scoreboard.registerNewTeam("altar_glow");
            altarTeam.setColor(ChatColor.YELLOW);
        }
        if (scoreboard.getTeam("upgrader_glow") == null) {
            Team upgraderTeam = scoreboard.registerNewTeam("upgrader_glow");
            upgraderTeam.setColor(ChatColor.LIGHT_PURPLE);
        }
        if (scoreboard.getTeam("reroller_glow") == null) {
            Team rerollerTeam = scoreboard.registerNewTeam("reroller_glow");
            rerollerTeam.setColor(ChatColor.AQUA);
        }
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        // Ensure teams exist before use
        createTeams();
        
        Item item = event.getEntity();
        ItemStack itemStack = item.getItemStack();
        ItemMeta meta = itemStack.getItemMeta();

        if (meta == null) return;

        PersistentDataContainer container = meta.getPersistentDataContainer();
        var scoreboard = plugin.getServer().getScoreboardManager();
        if (scoreboard == null) return;
        var mainScoreboard = scoreboard.getMainScoreboard();

        if (container.has(ItemKeys.namespaced(plugin, ItemKeys.KEY_ALTAR_ITEM), PersistentDataType.BYTE)) {
            item.setGlowing(true);
            Team team = mainScoreboard.getTeam("altar_glow");
            if (team != null) team.addEntry(item.getUniqueId().toString());
        } else if (container.has(ItemKeys.namespaced(plugin, ItemKeys.KEY_UPGRADER_LEVEL), PersistentDataType.INTEGER)) {
            item.setGlowing(true);
            Team team = mainScoreboard.getTeam("upgrader_glow");
            if (team != null) team.addEntry(item.getUniqueId().toString());
        } else if (container.has(ItemKeys.namespaced(plugin, ItemKeys.KEY_REROLLER), PersistentDataType.BYTE) ||
                   container.has(ItemKeys.namespaced(plugin, ItemKeys.KEY_ADVANCED_REROLLER), PersistentDataType.BYTE)) {
            item.setGlowing(true);
            Team team = mainScoreboard.getTeam("reroller_glow");
            if (team != null) team.addEntry(item.getUniqueId().toString());
        }
    }
}
