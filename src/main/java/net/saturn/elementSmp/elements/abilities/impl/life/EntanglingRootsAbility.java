package net.saturn.elementsmp.elements.abilities.impl.life;

import net.saturn.elementsmp.ElementSmp;
import net.saturn.elementsmp.config.Constants;
import net.saturn.elementsmp.config.MetadataKeys;
import net.saturn.elementsmp.elements.core.ElementContext;
import net.saturn.elementsmp.elements.abilities.BaseAbility;
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
    public static final String META_SINK = MetadataKeys.Life.ENTANGLED_SINK;
    public static final String META_RELEASE = MetadataKeys.Life.ENTANGLED_RELEASE;

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
        
        long durationMs = Constants.Duration.LIFE_ENTANGLE_MS;
        long stunUntil = System.currentTimeMillis() + durationMs;
        target.setMetadata(META_ENTANGLED, new FixedMetadataValue(plugin, stunUntil));

        if (target instanceof Player targetPlayer) {
            targetPlayer.sendMessage(ChatColor.RED + "You have been entangled by roots!");
        } else if (target instanceof Mob mob) {
            mob.setAware(false);
        }

        Location originalLoc = target.getLocation().clone();
        Location sinkLoc = originalLoc.clone();
        Location releaseLoc = originalLoc.clone();
        
        // Immobilize on the spot instead of sinking to avoid block interaction/collision bugs
        target.setMetadata(META_SINK, new FixedMetadataValue(plugin, sinkLoc));
        target.setMetadata(META_RELEASE, new FixedMetadataValue(plugin, releaseLoc));
        
        originalLoc.getWorld().playSound(originalLoc, Sound.BLOCK_ROOTS_BREAK, 1.5f, 0.5f);
        originalLoc.getWorld().spawnParticle(Particle.BLOCK, originalLoc, 50, 0.5, 0.5, 0.5, 0.1, org.bukkit.Material.MANGROVE_ROOTS.createBlockData());

        // Keep them on the spot
        target.setVelocity(new Vector(0, 0, 0));

        // Apply visual effects
        int durationTicks = (int) (durationMs / 50);
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, durationTicks, 4, false, false, true));

        final LivingEntity finalTarget = target;
        final boolean wasAllowFlight = (target instanceof Player p) ? p.getAllowFlight() : false;
        if (target instanceof Player p) {
            p.setAllowFlight(true);
        }

        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = durationTicks;

            @Override
            public void run() {
                if (!finalTarget.isValid() || finalTarget.isDead() || ticks >= maxTicks) {
                    finalTarget.removeMetadata(META_ENTANGLED, plugin);
                    
                    if (finalTarget.isValid() && !finalTarget.isDead()) {
                        if (finalTarget instanceof Mob mob) {
                            mob.setAware(true);
                        }
                        
                        // Final teleport to ensure they are at the correct location and synced
                        if (finalTarget.hasMetadata(META_RELEASE)) {
                            Location release = (Location) finalTarget.getMetadata(META_RELEASE).get(0).value();
                            if (release != null) {
                                // Keep the player's current rotation
                                Location finalLoc = release.clone();
                                finalLoc.setYaw(finalTarget.getLocation().getYaw());
                                finalLoc.setPitch(finalTarget.getLocation().getPitch());
                                finalTarget.teleport(finalLoc);
                            }
                        }
                    }
                    
                    finalTarget.removeMetadata(META_SINK, plugin);
                    finalTarget.removeMetadata(META_RELEASE, plugin);

                    if (finalTarget instanceof Player p) {
                        if (!wasAllowFlight && p.getGameMode() != org.bukkit.GameMode.CREATIVE && p.getGameMode() != org.bukkit.GameMode.SPECTATOR) {
                            p.setAllowFlight(false);
                        }
                        // Force a sync with the client to resolve "ghost blocks" or state corruption
                        p.updateInventory();
                    }
                    cancel();
                    return;
                }

                // Visual root particles
                if (ticks % 5 == 0) {
                    finalTarget.getWorld().spawnParticle(Particle.BLOCK, finalTarget.getLocation().add(0, 0.1, 0), 5, 0.3, 0.1, 0.3, 0.0, org.bukkit.Material.MANGROVE_ROOTS.createBlockData());
                }
                
                // Keep them at the location - only for non-players to avoid desync
                // Players are handled by PlayerMoveEvent in LifeListener
                if (!(finalTarget instanceof Player)) {
                    if (ticks % 2 == 0 && finalTarget.hasMetadata(META_SINK)) {
                        Location sink = (Location) finalTarget.getMetadata(META_SINK).get(0).value();
                        if (sink != null) {
                            Location targetLoc = sink.clone();
                            targetLoc.setYaw(finalTarget.getLocation().getYaw());
                            targetLoc.setPitch(finalTarget.getLocation().getPitch());
                            finalTarget.teleport(targetLoc);
                        }
                    }
                }
                
                finalTarget.setVelocity(new Vector(0, 0, 0));
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        return true;
    }
}
