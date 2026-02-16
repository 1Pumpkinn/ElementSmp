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

        // Immediate knockback
        for (LivingEntity target : player.getWorld().getNearbyLivingEntities(center, 5.5)) {
            if (target.equals(player)) continue;
            if (target instanceof Player tp && context.getTrustManager().isTrusted(player.getUniqueId(), tp.getUniqueId())) continue;
            Vector dir = target.getLocation().toVector().subtract(center.toVector());
            if (dir.lengthSquared() > 0.0001) {
                dir.normalize();
                Vector vel = dir.multiply(0.6);
                vel.setY(0.6);
                target.setVelocity(target.getVelocity().add(vel));
            }
        }

        // Particle shockwave effect
        createShockwave(player, center);

        return true;
    }

    private void createShockwave(Player player, Location center) {
        center.getWorld().playSound(center, Sound.ENTITY_WITHER_SHOOT, 0.7f, 0.5f);
        center.getWorld().playSound(center, Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1.0f, 0.2f);

        new BukkitRunnable() {
            final int duration = 12;
            final double maxRadius = 6.0;
            int ticks = 0;

            @Override
            public void run() {
                if (ticks > duration) {
                    center.getWorld().spawnParticle(Particle.SNOWFLAKE, center, 200, maxRadius, 0.5, maxRadius, 0.1);
                    center.getWorld().playSound(center, Sound.BLOCK_GLASS_BREAK, 1.2f, 0.2f);
                    cancel();
                    return;
                }

                double radius = (double) ticks / duration * maxRadius;

                for (double theta = 0; theta <= 2 * Math.PI; theta += Math.PI / 20) {
                    double x = radius * Math.cos(theta);
                    double z = radius * Math.sin(theta);
                    Location particleLoc = center.clone().add(x, 0.2, z);
                    center.getWorld().spawnParticle(Particle.SNOWFLAKE, particleLoc, 1, 0, 0, 0, 0);
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0, 1);
    }
}
