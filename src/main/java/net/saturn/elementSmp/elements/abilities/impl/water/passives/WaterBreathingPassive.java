package net.saturn.elementSmp.elements.abilities.impl.water.passives;

import net.saturn.elementSmp.elements.ElementType;
import net.saturn.elementSmp.managers.ElementManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Handles Water element passive: Drowning Immunity
 */
public class WaterBreathingPassive implements Listener {
    private final ElementManager elementManager;

    public WaterBreathingPassive(ElementManager elementManager) {
        this.elementManager = elementManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onWaterDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (elementManager.getPlayerElement(player) != ElementType.WATER) return;
        
        // Passive: Drowning Immunity
        if (event.getCause() == EntityDamageEvent.DamageCause.DROWNING) {
            event.setCancelled(true);
        }
    }
}
