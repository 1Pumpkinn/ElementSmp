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

public class FrostNovaAbility extends BaseAbility {
    private final ElementSmp plugin;

    public FrostNovaAbility(ElementSmp plugin) {
        super();
        this.plugin = plugin;
    }

    @Override
    public boolean execute(ElementContext context) {
        Player player = context.getPlayer();
        Location center = player.getLocation();

        // Sounds
        player.getWorld().playSound(center, Sound.ENTITY_EVOKER_CAST_SPELL, 1.0f, 1.0f);
        player.getWorld().playSound(center, Sound.BLOCK_GLASS_BREAK, 1.0f, 0.5f);
        player.getWorld().playSound(center, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.0f);

        // Damage and freeze logic
        for (LivingEntity entity : player.getWorld().getNearbyLivingEntities(center, 5.5)) {
            if (entity.equals(player)) continue;

            if (entity instanceof Player targetPlayer) {
                if (context.getTrustManager().isTrusted(player.getUniqueId(), targetPlayer.getUniqueId())) {
                    continue;
                }
            }

            entity.damage(6.0, player);
            entity.setFreezeTicks(140);
        }

        // Particle shockwave effect
        createShockwave(player, center);

        return true;
    }

    private void createShockwave(Player player, Location center) {
        // More impactful sounds
        center.getWorld().playSound(center, Sound.ENTITY_WITHER_SHOOT, 0.7f, 0.5f);
        center.getWorld().playSound(center, Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1.0f, 0.2f);

        new BukkitRunnable() {
            final int duration = 12; // Faster animation (was 20)
            final double maxRadius = 6.0;
            int ticks = 0;

            @Override
            public void run() {
                if (ticks > duration) {
                    // Final burst with fewer particles
                    center.getWorld().spawnParticle(Particle.SNOWFLAKE, center, 200, maxRadius, 0.5, maxRadius, 0.1);
                    center.getWorld().playSound(center, Sound.BLOCK_GLASS_BREAK, 1.2f, 0.2f);
                    cancel();
                    return;
                }

                double radius = (double) ticks / duration * maxRadius;
                double halfPi = Math.PI / 2;

                // Create a flatter, wider dome to reduce vertical obstruction
                for (double phi = Math.PI / 3; phi <= halfPi; phi += Math.PI / 12) { // Starts from 60 degrees, not 0
                    for (double theta = 0; theta <= 2 * Math.PI; theta += Math.PI / 20) {
                        double x = radius * Math.cos(theta) * Math.sin(phi);
                        double y = radius * Math.cos(phi); // Y is naturally smaller now
                        double z = radius * Math.sin(theta) * Math.sin(phi);

                        Location particleLoc = center.clone().add(x, y, z);

                        // Leading edge of the shockwave
                        if (ticks > 1 && (phi > halfPi - 0.2 || theta > 2 * Math.PI - 0.2)) {
                             center.getWorld().spawnParticle(Particle.END_ROD, particleLoc, 1, 0, 0, 0, 0);
                        }
                        
                        // Body of the shockwave
                        center.getWorld().spawnParticle(Particle.SNOWFLAKE, particleLoc, 1, 0, 0, 0, 0);

                        // Add some extra soul fire for a magical touch
                        if (Math.random() < 0.08) { // Slightly less frequent
                            center.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, particleLoc, 1, 0, 0, 0, 0);
                        }
                    }
                }
                
                // Lingering ground frost effect, triggers more often in the shorter timeframe
                if (ticks % 3 == 0) {
                    for (int i = 0; i < 360; i += 15) { // Less dense ground particles
                        double angle = Math.toRadians(i);
                        Location groundLoc = center.clone().add(radius * Math.cos(angle), 0.1, radius * Math.sin(angle));
                        if (groundLoc.getBlock().getType().isSolid()) {
                            center.getWorld().spawnParticle(Particle.CLOUD, groundLoc, 2, 0.3, 0, 0.3, 0.01);
                        }
                    }
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0, 1);
    }
}
