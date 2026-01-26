package net.saturn.elementSmp.elements.abilities.impl.water;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.elements.ElementContext;
import net.saturn.elementSmp.elements.abilities.BaseAbility;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class WaterPrisonAbility extends BaseAbility {
    private final ElementSmp plugin;

    // Metadata key for stun tracking
    public static final String META_WATER_PRISON = "water_prison_stunned";

    public WaterPrisonAbility(ElementSmp plugin) {
        super();
        this.plugin = plugin;
    }

    @Override
    public boolean execute(ElementContext context) {
        Player player = context.getPlayer();

        // --- Target detection (Entity Raycast) ---
        double range = 15;
        
        org.bukkit.util.RayTraceResult entityHit = player.getWorld().rayTraceEntities(
                player.getEyeLocation(),
                player.getEyeLocation().getDirection(),
                range,
                0.5,
                entity -> {
                    if (!(entity instanceof LivingEntity living)) return false;
                    if (living.equals(player)) return false;
                    if (living instanceof Player targetPlayer) {
                        if (context.getTrustManager().isTrusted(player.getUniqueId(), targetPlayer.getUniqueId())) return false;
                    }
                    return true;
                }
        );

        LivingEntity target = null;

        if (entityHit != null && entityHit.getHitEntity() instanceof LivingEntity living) {
            org.bukkit.util.RayTraceResult blockHit = player.getWorld().rayTraceBlocks(
                    player.getEyeLocation(),
                    player.getEyeLocation().getDirection(),
                    player.getEyeLocation().distance(living.getEyeLocation()),
                    org.bukkit.FluidCollisionMode.NEVER,
                    true
            );

            if (blockHit == null || blockHit.getHitBlock() == null) {
                target = living;
            }
        }

        if (target == null) {
            player.sendMessage(ChatColor.RED + "You must look at a valid entity to trap it!");
            return false;
        }

        final LivingEntity finalTarget = target;
        
        // Play sounds
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_1, 1.0f, 0.8f);
        finalTarget.getWorld().playSound(finalTarget.getLocation(), Sound.ENTITY_PLAYER_SPLASH, 1.0f, 1.0f);

        // Apply metadata for stun duration (5 seconds = 5000ms)
        long durationMs = 5000;
        long stunUntil = System.currentTimeMillis() + durationMs;
        finalTarget.setMetadata(META_WATER_PRISON, new FixedMetadataValue(plugin, stunUntil));

        // Enable flight for players to prevent "kick for flying"
        final boolean wasAllowFlight = (finalTarget instanceof Player p) ? p.getAllowFlight() : false;
        if (finalTarget instanceof Player p) {
            p.setAllowFlight(true);
        }

        // Particle and Drowning Effect Task
        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = 100; // 5 seconds

            @Override
            public void run() {
                if (!finalTarget.isValid() || finalTarget.isDead() || ticks >= maxTicks) {
                    if (finalTarget.isValid()) {
                        finalTarget.removeMetadata(META_WATER_PRISON, plugin);
                        if (finalTarget instanceof Player p) {
                            if (!wasAllowFlight && p.getGameMode() != org.bukkit.GameMode.CREATIVE && p.getGameMode() != org.bukkit.GameMode.SPECTATOR) {
                                p.setAllowFlight(false);
                            }
                        }
                    }
                    cancel();
                    return;
                }

                // Bubble wrapping effect
                Location loc = finalTarget.getLocation().add(0, 1, 0);
                double radius = 1.2;
                for (int i = 0; i < 5; i++) {
                    double angle = (ticks * 0.2 + (i * (Math.PI * 2 / 5))) % (2 * Math.PI);
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    double y = Math.sin(ticks * 0.1 + i) * 0.5;
                    
                    Location particleLoc = loc.clone().add(x, y, z);
                    finalTarget.getWorld().spawnParticle(Particle.BUBBLE, particleLoc, 1, 0, 0, 0, 0, null, true);
                    finalTarget.getWorld().spawnParticle(Particle.SPLASH, particleLoc, 1, 0, 0, 0, 0, null, true);
                }
                
                // Sphere effect
                if (ticks % 5 == 0) {
                    for (int i = 0; i < 20; i++) {
                        double phi = Math.acos(-1 + (2.0 * i) / 20);
                        double theta = Math.sqrt(20 * Math.PI) * phi;
                        
                        double x = radius * Math.cos(theta) * Math.sin(phi);
                        double y = radius * Math.sin(theta) * Math.sin(phi);
                        double z = radius * Math.cos(phi);
                        
                        finalTarget.getWorld().spawnParticle(Particle.BUBBLE, loc.clone().add(x, y, z), 1, 0, 0, 0, 0, null, true);
                    }
                }

                // Drowning logic: decrease air or deal damage
                if (finalTarget.getRemainingAir() > 0) {
                    finalTarget.setRemainingAir(Math.max(0, finalTarget.getRemainingAir() - 20));
                } else {
                    finalTarget.damage(1.0); // Deal 0.5 heart damage per tick when out of air
                }

                // Stun: reset velocity
                finalTarget.setVelocity(new Vector(0, 0, 0));

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        return true;
    }
}
