package net.saturn.elementSmp.elements.impl.life.listeners;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.elements.ElementType;
import net.saturn.elementSmp.elements.abilities.impl.life.EntanglingRootsAbility;
import net.saturn.elementSmp.managers.ElementManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;
import io.papermc.paper.event.entity.EntityMoveEvent;

public class LifeListener implements Listener {
    private final ElementSmp plugin;
    private final ElementManager elementManager;

    public LifeListener(ElementSmp plugin, ElementManager elementManager) {
        this.plugin = plugin;
        this.elementManager = elementManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.hasMetadata(EntanglingRootsAbility.META_ENTANGLED)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot break blocks while entangled in roots!");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.hasMetadata(EntanglingRootsAbility.META_ENTANGLED)) {
            if (event.getFrom().getX() != event.getTo().getX() || 
                event.getFrom().getY() != event.getTo().getY() || 
                event.getFrom().getZ() != event.getTo().getZ()) {
                
                event.setCancelled(true);
                player.setVelocity(new Vector(0, 0, 0));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityMove(EntityMoveEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player) return;

        if (entity.hasMetadata(EntanglingRootsAbility.META_ENTANGLED)) {
            event.setCancelled(true);
            entity.setVelocity(new Vector(0, 0, 0));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity().hasMetadata(EntanglingRootsAbility.META_ENTANGLED)) {
            // Prevent suffocation damage while buried
            if (event.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION) {
                event.setCancelled(true);
                return;
            }
            // Prevent knockback
            event.getEntity().setVelocity(new Vector(0, 0, 0));
        }
    }
}