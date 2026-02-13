package net.saturn.elementSmp.elements.abilities.impl.frost;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.elements.ElementContext;
import net.saturn.elementSmp.elements.abilities.BaseAbility;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

public class FrostBombAbility extends BaseAbility {
    private final ElementSmp plugin;

    public FrostBombAbility(ElementSmp plugin) {
        super();
        this.plugin = plugin;
    }

    @Override
    public boolean execute(ElementContext context) {
        Player player = context.getPlayer();
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SNOWBALL_THROW, 1.0f, 1.0f);

        new BukkitRunnable() {
            Location loc = player.getEyeLocation();
            Vector dir = player.getLocation().getDirection().normalize();
            double t = 0;

            public void run() {
                t += 1.5; // Faster projectile
                double x = dir.getX() * t;
                double y = dir.getY() * t;
                double z = dir.getZ() * t;
                loc.add(x, y, z);

                // Enhanced particle trail
                loc.getWorld().spawnParticle(Particle.CLOUD, loc, 5, 0.1, 0.1, 0.1, 0.01);
                loc.getWorld().spawnParticle(Particle.SNOWFLAKE, loc, 3, 0.1, 0.1, 0.1, 0);
                loc.getWorld().spawnParticle(Particle.END_ROD, loc, 1, 0, 0, 0, 0);


                if (t > 25 || loc.getBlock().getType().isSolid()) {
                    this.cancel();
                    // More powerful explosion effect
                    loc.getWorld().spawnParticle(Particle.EXPLOSION, loc, 2);
                    loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);

                    // Consistent knockback for all entities, including the player
                    for (Entity entity : loc.getWorld().getNearbyEntities(loc, 5, 5, 5)) {
                        if (entity instanceof LivingEntity) {
                            entity.setVelocity(new Vector(0, 0, 0)); // Reset velocity for a consistent boost
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    Vector direction = entity.getLocation().toVector().subtract(loc.toVector()).normalize();
                                    direction.setY(0.6); // A bit less vertical lift
                                    entity.setVelocity(direction.multiply(2.4)); // A tiny bit more force
                                }
                            }.runTaskLater(plugin, 1L);
                        }
                    }

                    // Turn ground to ice
                    Map<Block, Material> originalBlocks = new HashMap<>();
                    int radius = 4; // Slightly larger ice patch
                    for (int i = -radius; i <= radius; i++) {
                        for (int j = -radius; j <= radius; j++) {
                            for (int k = -radius; k <= radius; k++) {
                                if (Math.sqrt(i * i + j * j + k * k) <= radius) {
                                    Block block = loc.clone().add(i, j, k).getBlock();
                                    if (block.getType().isSolid() && block.getType() != Material.ICE && block.getType() != Material.AIR) {
                                        originalBlocks.put(block, block.getType());
                                        block.setType(Material.ICE);
                                    }
                                }
                            }
                        }
                    }

                    // Revert ice to original blocks
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            for (Map.Entry<Block, Material> entry : originalBlocks.entrySet()) {
                                entry.getKey().setType(entry.getValue());
                            }
                        }
                    }.runTaskLater(plugin, 100L); // 5 seconds
                }
                loc.subtract(x, y, z);
            }
        }.runTaskTimer(plugin, 0, 1);

        return true;
    }

}
