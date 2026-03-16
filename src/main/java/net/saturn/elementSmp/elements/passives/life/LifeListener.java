package net.saturn.elementsmp.elements.passives.life;

import net.saturn.elementsmp.ElementSmp;
import net.saturn.elementsmp.elements.abilities.impl.life.EntanglingRootsAbility;
import net.saturn.elementsmp.managers.ElementManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.metadata.FixedMetadataValue;
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
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.hasMetadata(EntanglingRootsAbility.META_ENTANGLED)) {
            Location from = event.getFrom();
            Location to = event.getTo();
            
            if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
                // Keep the 'to' rotation but use the 'from' position
                Location newTo = from.clone();
                newTo.setYaw(to.getYaw());
                newTo.setPitch(to.getPitch());
                event.setTo(newTo);
                
                player.setVelocity(new Vector(0, 0, 0));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (player.hasMetadata(EntanglingRootsAbility.META_ENTANGLED)) {
            if (event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
                // Allow the pearl, but update the stuck location
                Location newLoc = event.getTo();
                if (newLoc != null) {
                    // Update the sink location to the new spot, keeping the sink depth
                    double sinkDepth = Math.max(0.3, Math.min(0.7, player.getHeight() * 0.35));
                    Location newSink = newLoc.clone().subtract(0, sinkDepth, 0);
                    Location newRelease = newLoc.clone();
                    
                    player.setMetadata(EntanglingRootsAbility.META_SINK, new FixedMetadataValue(plugin, newSink));
                    player.setMetadata(EntanglingRootsAbility.META_RELEASE, new FixedMetadataValue(plugin, newRelease));
                }
            } else if (event.getCause() != PlayerTeleportEvent.TeleportCause.COMMAND && 
                       event.getCause() != PlayerTeleportEvent.TeleportCause.PLUGIN) {
                // Block other teleports (like chorus fruit) if you want, or just leave it.
                // For now, let's just ensure we handle the pearl as requested.
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

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        clearEntangledMetadata(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent event) {
        clearEntangledMetadata(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        clearEntangledMetadata(event.getPlayer());
    }

    private void clearEntangledMetadata(Player player) {
        if (player.hasMetadata(EntanglingRootsAbility.META_ENTANGLED)) {
            player.removeMetadata(EntanglingRootsAbility.META_ENTANGLED, plugin);
            player.removeMetadata(EntanglingRootsAbility.META_SINK, plugin);
            player.removeMetadata(EntanglingRootsAbility.META_RELEASE, plugin);
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
