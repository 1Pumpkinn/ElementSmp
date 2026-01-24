package net.saturn.elementSmp.elements.impl.life.listeners;

import net.saturn.elementSmp.elements.ElementType;
import net.saturn.elementSmp.elements.abilities.impl.life.EntanglingRootsAbility;
import net.saturn.elementSmp.managers.ElementManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;

public class  LifeListener implements Listener {
    private final ElementManager elementManager;

    public LifeListener(ElementManager elementManager) {
        this.elementManager = elementManager;
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (EntanglingRootsAbility.isEntangled(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot break blocks while entangled in roots!");
        }
    }
}