package net.saturn.elementSmp.elements.abilities.impl.air;

import net.saturn.elementSmp.config.Constants;
import net.saturn.elementSmp.elements.ElementContext;
import net.saturn.elementSmp.elements.abilities.BaseAbility;
import net.saturn.elementSmp.managers.ManaManager;
import net.saturn.elementSmp.managers.TrustManager;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class AirBlastAbility extends BaseAbility {
    private final net.saturn.elementSmp.ElementSmp plugin;

    public AirBlastAbility(net.saturn.elementSmp.ElementSmp plugin) {
        super();
        this.plugin = plugin;
    }

    @Override
    public boolean execute(ElementContext context) {
        Player player = context.getPlayer();

        // Launch the player upward
        player.setVelocity(player.getVelocity().setY(2.5));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1f, 0.5f);
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 10, 0.5, 0.5, 0.5, 0.1);

        final boolean wasAllowFlight = player.getAllowFlight();
        player.setAllowFlight(true);

        setActive(player, true);

        // Monitor flight and perform blast on landing
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    setActive(player, false);
                    cancel();
                    return;
                }

                // Increase gravity when falling
                double yVel = player.getVelocity().getY();
                if (yVel < -0.1) {
                    yVel -= 0.15;
                }

                // Air control: Steer in look direction
                Vector lookDir = player.getLocation().getDirection().normalize();
                double speed = 0.75;
                player.setVelocity(new Vector(lookDir.getX() * speed, yVel, lookDir.getZ() * speed));

                // Check for landing (wait a few ticks to ensure they left ground)
                if (ticks > 5 && player.isOnGround()) {
                    performBlast(context);
                    setActive(player, false);
                    if (!wasAllowFlight && player.getGameMode() != org.bukkit.GameMode.CREATIVE && player.getGameMode() != org.bukkit.GameMode.SPECTATOR) {
                        player.setAllowFlight(false);
                    }
                    cancel();
                    return;
                }

                // Timeout
                if (ticks > 100) {
                    setActive(player, false);
                    if (!wasAllowFlight && player.getGameMode() != org.bukkit.GameMode.CREATIVE && player.getGameMode() != org.bukkit.GameMode.SPECTATOR) {
                        player.setAllowFlight(false);
                    }
                    cancel();
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 1L, 1L);

        return true;
    }

    private void performBlast(ElementContext context) {
        Player player = context.getPlayer();
        
        // Launch all nearby players away, with particles

        double radius = Constants.Distance.AIR_BLAST_RADIUS;
        World w = player.getWorld();
        Location center = player.getLocation();

        // Particle ring
        for (int i = 0; i < 360; i += 10) {
            double rad = Math.toRadians(i);
            double x = Math.cos(rad) * 1.5;
            double z = Math.sin(rad) * 1.5;
            w.spawnParticle(Particle.CLOUD, center.clone().add(x, 0.2, z), 2, 0.0, 0.0, 0.0, 0.0, null, true);
        }
        // Animated particle ring that shoots outward
        new BukkitRunnable() {
            int tick = 0;
            @Override
            public void run() {
                double currentRadius = 1.5 + (tick * 0.8); // Expands outward
                if (currentRadius > 8.0) {
                    cancel();
                    return;
                }

                // Spawn particles in a ring
                for (int i = 0; i < 360; i += 10) {
                    double rad = Math.toRadians(i);
                    double x = Math.cos(rad) * currentRadius;
                    double z = Math.sin(rad) * currentRadius;

                    // Particles shrink as they move outward
                    int count = Math.max(1, 3 - tick/2);
                    w.spawnParticle(Particle.CLOUD, center.clone().add(x, 0.2, z), count, 0.0, 0.0, 0.0, 0.0, null, true);
                }
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        // Launch nearby entities
        for (LivingEntity e : player.getLocation().getNearbyLivingEntities(radius)) {
            if (e.equals(player)) continue;

            if (e instanceof Player targetPlayer) {
                if (context.getTrustManager().isTrusted(player.getUniqueId(), targetPlayer.getUniqueId())) continue;
            }

            Vector push = e.getLocation().toVector().subtract(center.toVector()).normalize().multiply(2.25).setY(1.5);
            e.setVelocity(push);

            // True Damage (2 hearts = 4.0 damage)
            double oldHealth = e.getHealth();
            double trueDamage = 4.0;

            e.setNoDamageTicks(0);
            e.damage(trueDamage); // Visuals

            // True Damage Correction
            double expectedHealth = oldHealth - trueDamage;
            if (e.getHealth() > expectedHealth) {
                if (e instanceof Player p && (p.getGameMode() == org.bukkit.GameMode.CREATIVE || p.getGameMode() == org.bukkit.GameMode.SPECTATOR)) {
                    // Do not reduce creative/spectator player health with true damage correction
                } else {
                    e.setHealth(Math.max(0, expectedHealth));
                }
            }
        }

        w.playSound(center, Sound.ENTITY_ENDER_DRAGON_FLAP, 1f, 1.5f);
    }
}
