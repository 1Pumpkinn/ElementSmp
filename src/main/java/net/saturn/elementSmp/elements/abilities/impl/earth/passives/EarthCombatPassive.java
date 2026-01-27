package net.saturn.elementSmp.elements.abilities.impl.earth.passives;

import net.saturn.elementSmp.elements.ElementType;
import net.saturn.elementSmp.managers.ElementManager;
import net.saturn.elementSmp.managers.TrustManager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Handles Earth element passive 1: 10% chance to give Slowness II on hit
 */
public class EarthCombatPassive implements Listener {
    private final ElementManager elementManager;
    private final TrustManager trustManager;

    public EarthCombatPassive(ElementManager elementManager, TrustManager trustManager) {
        this.elementManager = elementManager;
        this.trustManager = trustManager;
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof LivingEntity victim)) return;

        if (elementManager.getPlayerElement(attacker) != ElementType.EARTH) return;

        // Trust check
        if (victim instanceof Player targetPlayer) {
            if (trustManager.isTrusted(attacker.getUniqueId(), targetPlayer.getUniqueId())) return;
        }

        // 10% chance to apply Slowness II for 3 seconds
        if (Math.random() < 0.10) {
            victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));
        }
    }
}
