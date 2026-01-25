package net.saturn.elementSmp.elements.impl.water.listeners;

import net.saturn.elementSmp.data.PlayerData;
import net.saturn.elementSmp.elements.ElementType;
import net.saturn.elementSmp.managers.ElementManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public class WaterPassiveListener implements Listener {
    private final ElementManager elementManager;
    private final Plugin plugin;

    public WaterPassiveListener(ElementManager elementManager) {
        this.elementManager = elementManager;
        this.plugin = elementManager.getPlugin();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onWaterDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (elementManager.data(player.getUniqueId()).getCurrentElement() != ElementType.WATER) return;
        
        // Passive: Drowning Immunity
        if (event.getCause() == EntityDamageEvent.DamageCause.DROWNING) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void onTridentHitEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Trident trident)) return;
        if (!(trident.getShooter() instanceof Player shooter)) return;
        if (!(event.getEntity() instanceof LivingEntity victim)) return;
        if (shooter.equals(victim)) return;

        handlePull(trident, shooter, victim);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Trident trident)) return;
        if (!(trident.getShooter() instanceof Player shooter)) return;
        if (!(event.getHitEntity() instanceof LivingEntity victim)) return;
        if (shooter.equals(victim)) return;

        handlePull(trident, shooter, victim);
    }

    private void handlePull(Trident trident, Player shooter, LivingEntity victim) {
        // Check if shooter is Water element
        PlayerData shooterData = elementManager.data(shooter.getUniqueId());
        if (shooterData.getCurrentElement() != ElementType.WATER) return;

        // Check for Upgrade II
        if (shooterData.getUpgradeLevel(ElementType.WATER) < 2) return;

        // Check if trident has Loyalty
        ItemStack tridentItem = trident.getItemStack();
        boolean hasLoyalty = false;
        if (tridentItem.getType() == Material.TRIDENT) {
            if (tridentItem.containsEnchantment(Enchantment.LOYALTY) || 
                (tridentItem.hasItemMeta() && tridentItem.getItemMeta().hasEnchant(Enchantment.LOYALTY))) {
                hasLoyalty = true;
            }
        }
        
        if (!hasLoyalty) return;

        // Calculate pull vector: from victim to shooter
        Vector shooterPos = shooter.getLocation().toVector();
        Vector victimPos = victim.getLocation().toVector();
        Vector direction = shooterPos.subtract(victimPos);
        
        double distance = direction.length();
        if (distance > 1.0) {
            // Gentler pull: base 0.8 + scaling with distance (max 1.8)
            double strength = 0.8 + Math.min(distance * 0.1, 1.0);
            direction.normalize().multiply(strength);
            
            // Reduced Y boost to prevent excessive vertical movement
            direction.setY(0.4);

            // Schedule velocity set for next tick to avoid being overridden by damage knockback
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (victim.isValid() && !victim.isDead()) {
                    // Force the velocity
                    victim.setVelocity(direction);
                    
                    // Feedback effects
                    victim.getWorld().playSound(victim.getLocation(), Sound.ITEM_TRIDENT_HIT, 1f, 1f);
                    victim.getWorld().spawnParticle(Particle.SPLASH, victim.getLocation().add(0, 1, 0), 25, 0.3, 0.3, 0.3, 0.1);
                    victim.getWorld().spawnParticle(Particle.UNDERWATER, victim.getLocation().add(0, 1, 0), 15, 0.5, 0.5, 0.5, 0.05);
                    
                    if (shooter.isOnline()) {
                        shooter.getWorld().playSound(shooter.getLocation(), Sound.ENTITY_FISHING_BOBBER_RETRIEVE, 1f, 0.8f);
                        if (victim instanceof Player) {
                        } else {
                        }
                    }
                }
            }, 1L);
        }
    }
}
