package net.saturn.elementSmp.elements.abilities.impl.life;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.config.MetadataKeys;
import net.saturn.elementSmp.elements.ElementContext;
import net.saturn.elementSmp.elements.abilities.BaseAbility;
import org.bukkit.ChatColor;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import org.bukkit.entity.Mob;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class EntanglingRootsAbility extends BaseAbility {

    private final ElementSmp plugin;
    public static final String META_ENTANGLED = MetadataKeys.Life.ENTANGLED;

    public EntanglingRootsAbility(ElementSmp plugin) {
        super();
        this.plugin = plugin;
    }

    @Override
    public boolean execute(ElementContext context) {
        Player player = context.getPlayer();
        double range = 15;
        double maxCheckRange = 30;

        // 1. Check if there is ANY entity in line of sight within a larger range
        RayTraceResult farHit = player.getWorld().rayTraceEntities(
                player.getEyeLocation(),
                player.getEyeLocation().getDirection(),
                maxCheckRange,
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

        if (farHit != null && farHit.getHitEntity() instanceof LivingEntity living) {
            double dist = player.getEyeLocation().distance(living.getEyeLocation());
            
            if (dist > range) {
                player.sendMessage(ChatColor.RED + "You're too far away!");
                return false;
            }

            // Check for SOLID blocks only
            RayTraceResult blockHit = player.getWorld().rayTraceBlocks(
                    player.getEyeLocation(),
                    player.getEyeLocation().getDirection(),
                    dist,
                    FluidCollisionMode.NEVER,
                    true
            );

            if (blockHit == null || blockHit.getHitBlock() == null) {
                target = living;
            }
        }

        if (target == null) {
            player.sendMessage(ChatColor.RED + "You must look at a valid entity to entangle it!");
            return false;
        }

        player.sendMessage(ChatColor.GREEN + "You have entangled " + target.getName() + "!");
        
        long durationMs = 3000; // 3 seconds
        long stunUntil = System.currentTimeMillis() + durationMs;
        target.setMetadata(META_ENTANGLED, new FixedMetadataValue(plugin, stunUntil));

        if (target instanceof Player targetPlayer) {
            targetPlayer.sendMessage(ChatColor.RED + "You have been entangled by roots!");
        } else if (target instanceof Mob mob) {
            mob.setAware(false);
        }

        Location originalLoc = target.getLocation().clone();
        Location sinkLoc = originalLoc.clone().subtract(0, 1.2, 0);
        
        originalLoc.getWorld().playSound(originalLoc, Sound.BLOCK_ROOTS_BREAK, 1.5f, 0.5f);
        originalLoc.getWorld().spawnParticle(Particle.BLOCK, originalLoc, 50, 0.5, 0.5, 0.5, 0.1, org.bukkit.Material.MANGROVE_ROOTS.createBlockData());

        // Sink them into the ground
        target.teleport(sinkLoc);
        target.setVelocity(new Vector(0, 0, 0));

        // Apply visual effects
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 10, false, false, true));

        final LivingEntity finalTarget = target;

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (!finalTarget.isValid() || ticks >= 100) {
                    if (finalTarget.isValid()) {
                        finalTarget.removeMetadata(META_ENTANGLED, plugin);
                        if (finalTarget instanceof Mob mob) {
                            mob.setAware(true);
                        }
                        // Teleport back up safely
                        finalTarget.teleport(originalLoc);
                    }
                    cancel();
                    return;
                }

                // Visual root particles
                if (ticks % 5 == 0) {
                    finalTarget.getWorld().spawnParticle(Particle.BLOCK, originalLoc.clone().add(0, 0.1, 0), 5, 0.3, 0.1, 0.3, 0.0, org.bukkit.Material.MANGROVE_ROOTS.createBlockData());
                }
                
                // Keep them at the sink location
                if (ticks % 2 == 0) {
                    finalTarget.teleport(sinkLoc);
                }
                finalTarget.setVelocity(new Vector(0, 0, 0));
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        return true;
    }
}
