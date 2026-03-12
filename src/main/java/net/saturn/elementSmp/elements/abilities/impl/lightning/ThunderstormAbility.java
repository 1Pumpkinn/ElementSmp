package net.saturn.elementSmp.elements.abilities.impl.lightning;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.elements.ElementContext;
import net.saturn.elementSmp.elements.abilities.BaseAbility;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class ThunderstormAbility extends BaseAbility {
    private final ElementSmp plugin;
    private final Random random = new Random();

    public ThunderstormAbility(ElementSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(ElementContext context) {
        Player player = context.getPlayer();
        
        // Effects at the caster
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
        player.getWorld().spawnParticle(Particle.GLOW, player.getLocation(), 50, 2, 1, 2, 0.1);

        // Track entities who have been processed by this storm cast
        java.util.Set<java.util.UUID> struckEntities = new java.util.HashSet<>();

        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = 200; // 10 seconds

            @Override
            public void run() {
                if (!player.isOnline() || ticks >= maxTicks) {
                    struckEntities.clear();
                    cancel();
                    return;
                }

                // Visual storm effects around the caster
                if (ticks % 5 == 0) {
                    // Random position within 10 blocks of player
                    double angle = random.nextDouble() * 2 * Math.PI;
                    double radius = 3 + random.nextDouble() * 7;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    org.bukkit.Location stormLoc = player.getLocation().add(x, 0, z);
                    
                    // Visual lightning strike (no damage)
                    if (random.nextDouble() < 0.3) {
                        player.getWorld().strikeLightningEffect(stormLoc);
                    }
                    
                    // Dark particles to simulate storm clouds/energy
                    player.getWorld().spawnParticle(Particle.LARGE_SMOKE, player.getLocation().add(0, 3, 0), 10, 4, 0.5, 4, 0.05);
                    player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, player.getLocation().add(0, 2, 0), 5, 2, 1, 2, 0.1);
                }

                // Every second (20 ticks), check for nearby entities
                if (ticks % 20 == 0) {
                    for (LivingEntity target : player.getWorld().getNearbyLivingEntities(player.getLocation(), 15)) {
                        if (!isValidTarget(context, target)) continue;
                        
                        // Only strike an entity once per ability cast
                        if (struckEntities.contains(target.getUniqueId())) continue;

                        // 20% chance to be struck
                        if (random.nextDouble() < 0.20) {
                            struckEntities.add(target.getUniqueId());
                            strikeTarget(target);
                            
                            // 5% chance to be struck a second time immediately
                            if (random.nextDouble() < 0.05) {
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        if (target.isValid() && !target.isDead()) {
                                            strikeTarget(target);
                                        }
                                    }
                                }.runTaskLater(plugin, 10L); // 0.5 second delay for the second strike
                            }
                        }
                    }
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        return true;
    }

    private void strikeTarget(LivingEntity target) {
        // Show yellow particles around the player who is about to get struck
        target.getWorld().spawnParticle(Particle.DUST, target.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5, new Particle.DustOptions(Color.YELLOW, 1.5f));
        target.getWorld().playSound(target.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.5f, 2.0f);

        // Strike lightning after a short delay to allow particles to be seen
        new BukkitRunnable() {
            @Override
            public void run() {
                if (target.isValid() && !target.isDead()) {
                    target.getWorld().strikeLightning(target.getLocation());
                }
            }
        }.runTaskLater(plugin, 5L); // 0.25 second delay
    }
}
