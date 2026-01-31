package net.saturn.elementSmp.elements.abilities.impl.death;

import net.saturn.elementSmp.elements.ElementContext;
import net.saturn.elementSmp.elements.abilities.BaseAbility;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class ShadowStepAbility extends BaseAbility {
    private final net.saturn.elementSmp.ElementSmp plugin;

    public ShadowStepAbility(net.saturn.elementSmp.ElementSmp plugin) {
        super();
        this.plugin = plugin;
    }

    @Override
    public boolean execute(ElementContext context) {
        Player player = context.getPlayer();
        Location startLoc = player.getEyeLocation();
        
        // Initial projectile settings (Like an arrow)
        Vector velocity = startLoc.getDirection().multiply(1.6);
        final Location currentLocation = startLoc.clone();
        final Vector currentVelocity = velocity.clone();
        final Vector gravity = new Vector(0, -0.06, 0); // Gravity strength
        final double drag = 0.99; // Air resistance

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 0.5f);

        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = 100; // Increased to 5 seconds to ensure it hits the ground

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }

                if (ticks >= maxTicks) {
                    // Stop if it takes too long without hitting anything, but don't TP mid-air
                    cancel();
                    return;
                }

                // Raytrace to check for collisions in the next step
                double speed = currentVelocity.length();
                if (speed > 0) {
                    org.bukkit.util.RayTraceResult result = currentLocation.getWorld().rayTraceBlocks(
                        currentLocation, 
                        currentVelocity, 
                        speed, 
                        FluidCollisionMode.NEVER, 
                        true
                    );

                    if (result != null && result.getHitBlock() != null) {
                        // Collision! Teleport to hit position (slightly offset from the wall/floor)
                        Location hitLoc = result.getHitPosition().toLocation(currentLocation.getWorld());
                        Vector offset = result.getHitBlockFace().getDirection().multiply(0.2);
                        hitLoc.add(offset);
                        finish(hitLoc);
                        cancel();
                        return;
                    }
                }

                // Update position and velocity
                currentLocation.add(currentVelocity);
                currentVelocity.add(gravity);
                currentVelocity.multiply(drag);

                // Particles
                currentLocation.getWorld().spawnParticle(Particle.SQUID_INK, currentLocation, 15, 0.1, 0.1, 0.1, 0.05);
                currentLocation.getWorld().spawnParticle(Particle.SMOKE, currentLocation, 8, 0.05, 0.05, 0.05, 0.02);
                
                // Travel sound
                if (ticks % 3 == 0) {
                    currentLocation.getWorld().playSound(currentLocation, Sound.ENTITY_ENDERMAN_AMBIENT, 0.4f, 2.0f);
                }

                ticks++;
            }

            private void finish(Location target) {
                // Keep player's current looking direction
                target.setYaw(player.getLocation().getYaw());
                target.setPitch(player.getLocation().getPitch());
                
                player.teleport(target);
                spawnArrivalParticles(target);
                player.getWorld().playSound(target, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
            }
        }.runTaskTimer(plugin, 0L, 1L);

        return true;
    }

    private void spawnArrivalParticles(Location loc) {
        loc.getWorld().spawnParticle(Particle.SQUID_INK, loc.clone().add(0, 1, 0), 60, 0.4, 0.6, 0.4, 0.1);
        loc.getWorld().spawnParticle(Particle.SMOKE, loc.clone().add(0, 1, 0), 40, 0.3, 0.5, 0.3, 0.05);
    }
}
