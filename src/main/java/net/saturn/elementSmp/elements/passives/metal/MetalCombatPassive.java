package net.saturn.elementsmp.elements.passives.metal;

import net.saturn.elementsmp.elements.core.ElementType;
import net.saturn.elementsmp.managers.ElementManager;
import net.saturn.elementsmp.managers.TrustManager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Handles Metal element passive: 10% chance to give Weakness II on hit
 */
public class MetalCombatPassive implements Listener {
    private final ElementManager elementManager;
    private final TrustManager trustManager;

    public MetalCombatPassive(ElementManager elementManager, TrustManager trustManager) {
        this.elementManager = elementManager;
        this.trustManager = trustManager;
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof LivingEntity victim)) return;

        if (elementManager.getPlayerElement(attacker) != ElementType.METAL) return;

        // Trust check
        if (victim instanceof Player targetPlayer) {
            if (trustManager.isTrusted(attacker.getUniqueId(), targetPlayer.getUniqueId())) return;
        }

    }
}
