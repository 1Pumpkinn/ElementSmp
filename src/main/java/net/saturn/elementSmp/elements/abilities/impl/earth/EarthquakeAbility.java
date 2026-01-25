package net.saturn.elementSmp.elements.abilities.impl.earth;

import net.saturn.elementSmp.elements.ElementContext;
import net.saturn.elementSmp.elements.abilities.BaseAbility;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;

public class EarthquakeAbility extends BaseAbility {

    public EarthquakeAbility(net.saturn.elementSmp.ElementSmp plugin) {
        super();
    }

    @Override
    public boolean execute(ElementContext context) {
        Player player = context.getPlayer();
        net.saturn.elementSmp.ElementSmp plugin = (net.saturn.elementSmp.ElementSmp) org.bukkit.Bukkit.getPluginManager().getPlugin("ElementSmp");
        
        double maxRadius = 7.0;
        Collection<Entity> nearbyEntities = player.getWorld().getNearbyEntities(player.getLocation(), maxRadius, maxRadius, maxRadius);
        
        boolean hitAny = false;
        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                LivingEntity target = (LivingEntity) entity;
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 5 * 20, 2));
                target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 5 * 20, 0));
                hitAny = true;
            }
        }

        if (hitAny) {
            final org.bukkit.Location center = player.getLocation();
            final org.bukkit.World world = player.getWorld();
            
            // Initial Impact Sounds
            world.playSound(center, Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 1.5f, 0.5f);
            world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.5f);
            world.playSound(center, Sound.BLOCK_STONE_PLACE, 2.0f, 0.5f);

            // Advanced Earthquake Animation
            new org.bukkit.scheduler.BukkitRunnable() {
                double currentRadius = 0.5;
                final double step = 0.7;
                int ticks = 0;

                @Override
                public void run() {
                    if (currentRadius > maxRadius) {
                        cancel();
                        return;
                    }

                    // Spawn particles in a dense circle
                    for (int i = 0; i < 360; i += 10) {
                        double angle = Math.toRadians(i);
                        double x = Math.cos(angle) * currentRadius;
                        double z = Math.sin(angle) * currentRadius;
                        org.bukkit.Location particleLoc = center.clone().add(x, 0.05, z);

                        // Ground Cracks (Block particles)
                        world.spawnParticle(Particle.BLOCK, particleLoc, 3, 0.2, 0.1, 0.2, 0.05, org.bukkit.Material.DIRT.createBlockData());
                        world.spawnParticle(Particle.BLOCK, particleLoc, 2, 0.1, 0.05, 0.1, 0.02, org.bukkit.Material.COARSE_DIRT.createBlockData());
                        
                        // Thick Dust Clouds (Moving out)
                        if (ticks % 2 == 0) {
                            world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, particleLoc, 1, 0.15, 0.1, 0.15, 0.02);
                        }
                    }

                    // Periodic Earth Spikes / Pillars
                    if (ticks % 3 == 0) {
                        for (int angle = 0; angle < 360; angle += 60) {
                            double rad = Math.toRadians(angle + (ticks * 10)); // Rotating offset
                            double x = Math.cos(rad) * currentRadius;
                            double z = Math.sin(rad) * currentRadius;
                            
                            for (double h = 0; h <= 1.2; h += 0.4) {
                                org.bukkit.Location pillarLoc = center.clone().add(x, h, z);
                                world.spawnParticle(Particle.BLOCK, pillarLoc, 4, 0.1, 0.1, 0.1, 0.01, org.bukkit.Material.ROOTED_DIRT.createBlockData());
                            }
                            world.playSound(center.clone().add(x, 0, z), Sound.BLOCK_GRASS_BREAK, 0.8f, 0.5f);
                        }
                    }

                    currentRadius += step;
                    ticks++;
                }
            }.runTaskTimer(plugin, 0L, 1L);

            return true;
        } else {
            player.sendMessage(ChatColor.RED + "No enemies nearby!");
            return false;
        }
    }
}
