package net.saturn.elementsmp.elements.passives.lightning;

import net.saturn.elementsmp.ElementSmp;
import net.saturn.elementsmp.elements.core.ElementType;
import net.saturn.elementsmp.managers.ElementManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

/**
 * Handles Lightning element passive effects.
 * Passives: Permanent Speed I and 10% chance to strike lightning on hit (at Upgrade II).
 * 
 * Note: These are also monitored by EffectService for persistence.
 */
public class LightningPassive implements Listener {

    private final ElementSmp plugin;
    private final ElementManager elementManager;
    private final Random random = new Random();

    public LightningPassive(ElementSmp plugin, ElementManager elementManager) {
        this.plugin = plugin;
        this.elementManager = elementManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (elementManager.getPlayerElement(player) == ElementType.LIGHTNING) {
            applyPassives(player);
        }
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.MONITOR, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof LivingEntity victim)) return;

        if (elementManager.getPlayerElement(attacker) != ElementType.LIGHTNING) return;
        
        var playerData = elementManager.data(attacker.getUniqueId());
        if (playerData.getUpgradeLevel(ElementType.LIGHTNING) < 2) return;

        // Use random.nextInt(100) for a clearer 10% chance (0-9 inclusive)
        // MONITOR priority and ignoreCancelled=true ensures we only strike on successful hits
        if (random.nextInt(100) < 10) {
            // strikeLightning strikes and deals damage, strikeLightningEffect is just visual
            // Using strikeLightning for the actual passive effect
            victim.getWorld().strikeLightning(victim.getLocation());
        }
    }

    private void applyPassives(Player player) {
        // Speed I (Permanent)
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, 0, true, false, false));
    }
}
