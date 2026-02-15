package net.saturn.elementSmp.elements.abilities.impl.frost;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.elements.ElementContext;
import net.saturn.elementSmp.elements.abilities.BaseAbility;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class FrostShurikenAbility extends BaseAbility {
    private final ElementSmp plugin;

    public FrostShurikenAbility(ElementSmp plugin) {
        super();
        this.plugin = plugin;
    }

    @Override
    public boolean execute(ElementContext context) {
        Player player = context.getPlayer();
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.0f);
        launchShuriken(player, context);
        return true;
    }

    private void launchShuriken(Player player, ElementContext context) {
        Location startLoc = player.getEyeLocation();
        Vector velocity = startLoc.getDirection().multiply(1.8);
        final Location currentLocation = startLoc.clone();
        final Vector currentVelocity = velocity.clone();
        final Vector gravity = new Vector(0, -0.05, 0);
        final double drag = 0.99;

        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = 100;
            double rotation = 0;

            @Override
            public void run() {
                if (!player.isOnline() || ticks >= maxTicks) {
                    cancel();
                    return;
                }

                // Block Collision detection
                if (currentLocation.getBlock().getType().isSolid()) {
                    currentLocation.getWorld().spawnParticle(Particle.SNOWFLAKE, currentLocation, 40, 0.8, 0.8, 0.8, 0.2);
                    cancel();
                    return;
                }

                // Entity collision
                for (LivingEntity entity : currentLocation.getWorld().getNearbyLivingEntities(currentLocation, 2.0)) {
                    if (entity.equals(player)) continue;

                    // Trust check
                    if (entity instanceof Player targetPlayer) {
                        if (context.getTrustManager().isTrusted(player.getUniqueId(), targetPlayer.getUniqueId())) {
                            continue;
                        }
                    }

                    if (entity.getBoundingBox().expand(0.8).contains(currentLocation.toVector())) {
                        entity.damage(8.0, player);
                        entity.setFreezeTicks(200);
                        currentLocation.getWorld().spawnParticle(Particle.SNOWFLAKE, currentLocation, 40, 0.8, 0.8, 0.8, 0.2);
                        cancel();
                        return; // Hit only one target
                    }
                }

                // Update position and velocity
                currentLocation.add(currentVelocity);
                currentVelocity.add(gravity);
                currentVelocity.multiply(drag);

                // Particle effect
                rotation += Math.PI / 8;
                spawnShurikenParticle(currentLocation, rotation);

                ticks++;
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    private void spawnShurikenParticle(Location location, double rotation) {
        // Center of the shuriken
        location.getWorld().spawnParticle(Particle.SNOWFLAKE, location, 15, 0.2, 0.2, 0.2, 0.02);
        location.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, location, 5, 0.1, 0.1, 0.1, 0);
    
        // The 4 blades
        for (int i = 0; i < 4; i++) {
            double bladeAngle = i * Math.PI / 2 + rotation;
            Vector direction = new Vector(Math.cos(bladeAngle), 0, Math.sin(bladeAngle));
    
            // Create a line of particles for each blade
            for (double length = 0.4; length <= 1.4; length += 0.2) {
                Vector offset = direction.clone().multiply(length);
                Location particleLoc = location.clone().add(offset);
    
                // Main blade particle
                location.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, particleLoc, 1, 0, 0, 0, 0);
    
                // Add some extra frost/ice particles along the blade
                if (length > 0.8) {
                    location.getWorld().spawnParticle(Particle.SNOWFLAKE, particleLoc, 1, 0, 0, 0, 0);
                }
            }
            
            // Tip of the blade - make it sharper
            Vector tipOffset = direction.clone().multiply(1.5);
            Location tipLoc = location.clone().add(tipOffset);
            location.getWorld().spawnParticle(Particle.END_ROD, tipLoc, 1, 0, 0, 0, 0);
            location.getWorld().spawnParticle(Particle.CRIT, tipLoc, 1, 0, 0, 0, 0);
        }
    
        // Add some of those floating star particles from the image
        double starAngle = rotation * 2; // Spin faster
        Vector starOffset = new Vector(Math.cos(starAngle) * 2.0, 0.5, Math.sin(starAngle) * 2.0);
        location.getWorld().spawnParticle(Particle.FIREWORK, location.clone().add(starOffset), 1, 0, 0, 0, 0);
        
        starAngle = rotation * 2 + Math.PI; // The one on the other side
        starOffset = new Vector(Math.cos(starAngle) * 2.0, 0.5, Math.sin(starAngle) * 2.0);
        location.getWorld().spawnParticle(Particle.FIREWORK, location.clone().add(starOffset), 1, 0, 0, 0, 0);
    }
}
