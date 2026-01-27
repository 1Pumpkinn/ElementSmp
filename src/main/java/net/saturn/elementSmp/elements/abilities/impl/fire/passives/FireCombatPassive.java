package net.saturn.elementSmp.elements.abilities.impl.fire.passives;

import net.saturn.elementSmp.elements.ElementType;
import net.saturn.elementSmp.managers.ElementManager;
import net.saturn.elementSmp.managers.TrustManager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Handles Fire element combat interactions
 * Fire Upside 2: Apply fire aspect when hitting enemies
 */
public class FireCombatPassive implements Listener {
    private final ElementManager elementManager;
    private final TrustManager trustManager;

    public FireCombatPassive(ElementManager elementManager, TrustManager trustManager) {
        this.elementManager = elementManager;
        this.trustManager = trustManager;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // Check if damager is a Fire element player
        if (!(event.getDamager() instanceof Player damager)) {
            return;
        }

        if (elementManager.getPlayerElement(damager) != ElementType.FIRE) {
            return;
        }

        // Check if they have Upgrade 2
        if (elementManager.data(damager.getUniqueId()).getCurrentElementUpgradeLevel() < 2) {
            return;
        }

        // Don't apply to trusted players or self
        if (event.getEntity() instanceof LivingEntity victim) {
            if (victim.equals(damager)) return;
            if (victim instanceof Player targetPlayer) {
                if (trustManager.isTrusted(damager.getUniqueId(), targetPlayer.getUniqueId())) return;
            }
        } else {
            return;
        }

        // Apply fire aspect (set entity on fire for 4 seconds)
        event.getEntity().setFireTicks(80); // 80 ticks = 4 seconds
    }
}
