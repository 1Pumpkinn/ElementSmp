package net.saturn.elementSmp.elements.impl.death.listeners;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.elements.ElementType;
import net.saturn.elementSmp.managers.ElementManager;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public class DeathPassiveListener implements Listener {

    private final ElementManager elementManager;

    public DeathPassiveListener(ElementSmp plugin, ElementManager elementManager) {
        this.elementManager = elementManager;
    }

    @EventHandler
    public void onPlayerKill(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        if (killer != null && elementManager.getPlayerElement(killer) == ElementType.DEATH) {
            // Passive 1: Soul Harvest - Restores 2 hearts (4 health points)
            double maxHealth = killer.getAttribute(Attribute.MAX_HEALTH).getValue();
            double newHealth = Math.min(maxHealth, killer.getHealth() + 4.0);
            killer.setHealth(newHealth);
            killer.sendMessage(ChatColor.DARK_RED + "Soul Harvest! You restored 2 hearts.");
        }
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHit(org.bukkit.event.entity.EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof org.bukkit.entity.LivingEntity victim)) return;
        if (elementManager.getPlayerElement(attacker) != ElementType.DEATH) return;

        // Passive 2: Void's Pull - Pull entities when hitting with sword or axe
        org.bukkit.inventory.ItemStack item = attacker.getInventory().getItemInMainHand();
        String typeName = item.getType().name();
        if (typeName.endsWith("_SWORD") || typeName.endsWith("_AXE")) {
            org.bukkit.util.Vector pullDirection = attacker.getLocation().toVector().subtract(victim.getLocation().toVector()).normalize();
            pullDirection.setY(0.2);
            victim.setVelocity(pullDirection.multiply(1.2));
            
            victim.getWorld().spawnParticle(org.bukkit.Particle.PORTAL, victim.getLocation().add(0, 1, 0), 20, 0.2, 0.2, 0.2, 0.1);
        }
    }
}
