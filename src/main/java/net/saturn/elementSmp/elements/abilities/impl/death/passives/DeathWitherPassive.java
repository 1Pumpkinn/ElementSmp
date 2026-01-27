package net.saturn.elementSmp.elements.abilities.impl.death.passives;

import net.saturn.elementSmp.elements.ElementType;
import net.saturn.elementSmp.managers.ElementManager;
import net.saturn.elementSmp.managers.TrustManager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class DeathWitherPassive implements Listener {

    private final ElementManager elementManager;
    private final TrustManager trustManager;

    public DeathWitherPassive(ElementManager elementManager, TrustManager trustManager) {
        this.elementManager = elementManager;
        this.trustManager = trustManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof LivingEntity victim)) return;
        if (elementManager.getPlayerElement(attacker) != ElementType.DEATH) return;

        // Passive 1: 10% wither effect when hitting an entity
        if (victim instanceof Player targetPlayer) {
            if (trustManager.isTrusted(attacker.getUniqueId(), targetPlayer.getUniqueId())) return;
        }

        if (Math.random() < 0.10) {
            victim.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 1)); // 5 seconds of Wither II
        }
    }
}
