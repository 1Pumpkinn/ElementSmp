package net.saturn.elementSmp.elements.impl.water.listeners;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.elements.abilities.impl.water.WaterPrisonAbility;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;
import io.papermc.paper.event.entity.EntityMoveEvent;

public class WaterPrisonListener implements Listener {
    private final ElementSmp plugin;

    public WaterPrisonListener(ElementSmp plugin) {
        this.plugin = plugin;
    }

    /**
     * Prevent trapped players from moving
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (player.hasMetadata(WaterPrisonAbility.META_WATER_PRISON)) {
            long stunUntil = player.getMetadata(WaterPrisonAbility.META_WATER_PRISON).get(0).asLong();

            if (System.currentTimeMillis() < stunUntil) {
                // Cancel movement if it's not just a head rotation
                if (event.getFrom().getX() != event.getTo().getX() ||
                        event.getFrom().getY() != event.getTo().getY() ||
                        event.getFrom().getZ() != event.getTo().getZ()) {
                    event.setCancelled(true);
                    player.setVelocity(new Vector(0, 0, 0));
                }
            } else {
                player.removeMetadata(WaterPrisonAbility.META_WATER_PRISON, plugin);
            }
        }
    }

    /**
     * Prevent trapped entities from moving
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityMove(EntityMoveEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player) return;

        if (entity.hasMetadata(WaterPrisonAbility.META_WATER_PRISON)) {
            long stunUntil = entity.getMetadata(WaterPrisonAbility.META_WATER_PRISON).get(0).asLong();

            if (System.currentTimeMillis() < stunUntil) {
                event.setCancelled(true);
                entity.setVelocity(new Vector(0, 0, 0));
            } else {
                entity.removeMetadata(WaterPrisonAbility.META_WATER_PRISON, plugin);
            }
        }
    }
}
