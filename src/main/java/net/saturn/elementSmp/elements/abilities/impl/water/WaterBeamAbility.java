package net.saturn.elementSmp.elements.abilities.impl.water;

import net.saturn.elementSmp.elements.ElementContext;
import net.saturn.elementSmp.elements.abilities.BaseAbility;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class WaterBeamAbility extends BaseAbility {
    private final net.saturn.elementSmp.ElementSmp plugin;

    public WaterBeamAbility(net.saturn.elementSmp.ElementSmp plugin) {
        super();
        this.plugin = plugin;
    }
    
    @Override
    public boolean execute(ElementContext context) {
        Player player = context.getPlayer();

        player.getWorld().playSound(player.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_3, 1f, 1.2f);

        setActive(player, true);

        new BukkitRunnable() {
            int ticks = 0;
            double totalDamageDealt = 0;
            @Override
            public void run() {
                // Ability ends after 10 seconds or 5 hearts of damage
                if (!player.isOnline() || ticks >= 200 || totalDamageDealt >= 10) {
                    setActive(player, false);
                    cancel();
                    return;
                }
                Vector dir = player.getLocation().getDirection().normalize();

                // Apply damage every 0.25 seconds
                if (ticks % 5 == 0) {
                    Location chestLoc = player.getLocation().add(0, 1.2, 0);
                    
                    // Check for blocks in the way first, but allow passage through small blocks
                    double maxDistance = 20.0;
                    for (double d = 0; d <= 20.0; d += 0.5) {
                        Location checkLoc = chestLoc.clone().add(dir.clone().multiply(d));
                        Block block = checkLoc.getBlock();
                        if (!isPassableBlock(block)) {
                            maxDistance = d;
                            break;
                        }
                    }
                    
                    // Now trace entities but only up to the nearest block
                    RayTraceResult r = player.getWorld().rayTraceEntities(chestLoc, dir, maxDistance,
                            entity -> {
                                if (!(entity instanceof LivingEntity living)) return false;
                                if (living.equals(player)) return false;
                                if (living instanceof Player targetPlayer) {
                                    if (context.getTrustManager().isTrusted(player.getUniqueId(), targetPlayer.getUniqueId())) return false;
                                }
                                return true;
                            });
                    if (r != null && r.getHitEntity() instanceof LivingEntity le) {
                        // Apply knockback with slight upward component
                            Vector knockback = le.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
                            knockback.setY(0.2);
                            knockback = knockback.multiply(0.8);
                            le.setVelocity(knockback);

                            if (totalDamageDealt < 10) {
                                double damageAmount = 0.5;
                                if (totalDamageDealt + damageAmount > 10) {
                                    damageAmount = 10 - totalDamageDealt;
                                }
                                // Instead of just subtracting health, use true Bukkit damage
                                le.damage(damageAmount, player);
                                totalDamageDealt += damageAmount;

                                Location hit = r.getHitPosition().toLocation(player.getWorld());

                                try {
									if (le instanceof Player) {
										player.getWorld().spawnParticle(Particle.SPLASH, hit, 15, 0.3, 0.3, 0.3, 0.2, null, true);
										player.getWorld().spawnParticle(Particle.BUBBLE_POP, hit, 10, 0.2, 0.2, 0.2, 0.1, null, true);
                                        player.getWorld().playSound(hit, Sound.ENTITY_PLAYER_SPLASH, 0.8f, 1.5f);

                                        // Create circular water ring effect
                                        for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 4) {
                                            double x = Math.cos(angle) * 0.5;
                                            double z = Math.sin(angle) * 0.5;
											Location ringLoc = hit.clone().add(x, 0.1, z);
											player.getWorld().spawnParticle(Particle.BUBBLE, ringLoc, 1, 0.05, 0.05, 0.05, 0.0, null, true);
                                        }
									} else {
										player.getWorld().spawnParticle(Particle.BUBBLE_COLUMN_UP, hit, 3, 0.1, 0.1, 0.1, 0.0, null, true);
                                    }
                                } catch (Exception e) {
                                    // Ignore particle errors
                                }
                            }
                        }
                    }

                Location eyeLoc = player.getEyeLocation();
                Vector direction = eyeLoc.getDirection();
                
                double maxBeamDistance = 20.0;
                double particleDistance = 0.5;
                
                for (double d = 0; d <= maxBeamDistance; d += particleDistance) {
                    Location particleLoc = eyeLoc.clone().add(direction.clone().multiply(d));
                    
                    if (!isPassableBlock(particleLoc.getBlock())) {
                        break;
                    }
                    
						if (ticks % 2 == 0) {
							player.getWorld().spawnParticle(Particle.SPLASH, particleLoc, 1, 0.05, 0.05, 0.05, 0.01, null, true);
							
							if (d % 2 < 0.5) {
								player.getWorld().spawnParticle(Particle.BUBBLE_POP, particleLoc, 1, 0.05, 0.05, 0.05, 0.01, null, true);
							}
                    }
                }
                
                ticks++;
            }
        }.runTaskTimer(context.getPlugin(), 0L, 1L);
        
        return true;
    }
    
    private boolean isPassableBlock(Block block) {
        Material type = block.getType();
        return !type.isSolid() || type == Material.WATER || type == Material.SNOW ||
               type.name().contains("GRASS") || type.name().contains("FLOWER");
    }
}
