package net.saturn.elementSmp.elements.abilities.impl.death;

import net.saturn.elementSmp.elements.ElementContext;
import net.saturn.elementSmp.elements.abilities.BaseAbility;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;
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
        
        // Find the target location (max 20 blocks)
        Location targetLoc = findTargetLocation(player, 20);
        
        Vector direction = targetLoc.toVector().subtract(startLoc.toVector());
        double distance = direction.length();
        
        if (distance < 0.5) {
            // If we're already basically at the target, just teleport immediately
            player.teleport(targetLoc);
            spawnArrivalParticles(targetLoc);
            return true;
        }
        
        Vector normalizedDir = direction.clone().normalize();

        // Particle travel settings
        double speed = 1.2; // blocks per tick (about 24 blocks per second)
        int totalTicks = (int) (distance / speed);
        if (totalTicks <= 0) totalTicks = 1;
        
        final int finalTotalTicks = totalTicks;
        final Location finalTarget = targetLoc;
        final Vector finalDir = normalizedDir;
        final double finalDist = distance;

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 0.5f);

        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }

                if (tick >= finalTotalTicks) {
                    // REACHED THE POS - TELEPORT NOW
                    
                    // Update target location with player's CURRENT rotation
                    finalTarget.setYaw(player.getLocation().getYaw());
                    finalTarget.setPitch(player.getLocation().getPitch());
                    
                    player.teleport(finalTarget);
                    spawnArrivalParticles(finalTarget);
                    player.getWorld().playSound(finalTarget, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                    cancel();
                    return;
                }

                // Calculate current particle position
                double currentDist = ((double) tick / finalTotalTicks) * finalDist;
                Location particleLoc = startLoc.clone().add(finalDir.clone().multiply(currentDist));
                
                // Spawn the traveling particles (Black square like)
                player.getWorld().spawnParticle(Particle.SQUID_INK, particleLoc, 25, 0.15, 0.15, 0.15, 0.05);
                player.getWorld().spawnParticle(Particle.SMOKE, particleLoc, 10, 0.1, 0.1, 0.1, 0.02);
                
                // Sound during travel
                if (tick % 2 == 0) {
                    player.getWorld().playSound(particleLoc, Sound.ENTITY_ENDERMAN_AMBIENT, 0.4f, 2.0f);
                }

                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        return true;
    }

    private Location findTargetLocation(Player player, int maxDistance) {
        Location start = player.getEyeLocation();
        Vector dir = start.getDirection();
        
        BlockIterator iterator = new BlockIterator(player.getWorld(), start.toVector(), dir, 0, maxDistance);
        Block lastSafe = start.getBlock();
        
        while (iterator.hasNext()) {
            Block next = iterator.next();
            if (next.getType().isSolid()) {
                break;
            }
            lastSafe = next;
        }
        
        Location target = lastSafe.getLocation().add(0.5, 0.1, 0.5);
        return target;
    }

    private void spawnArrivalParticles(Location loc) {
        loc.getWorld().spawnParticle(Particle.SQUID_INK, loc.clone().add(0, 1, 0), 60, 0.4, 0.6, 0.4, 0.1);
        loc.getWorld().spawnParticle(Particle.SMOKE, loc.clone().add(0, 1, 0), 40, 0.3, 0.5, 0.3, 0.05);
    }
}
