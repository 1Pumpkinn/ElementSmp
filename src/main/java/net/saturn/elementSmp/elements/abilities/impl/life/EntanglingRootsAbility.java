package net.saturn.elementSmp.elements.abilities.impl.life;

import net.saturn.elementSmp.ElementSmp;
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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class EntanglingRootsAbility extends BaseAbility {

    private final ElementSmp plugin;
    private static final Set<UUID> entangledPlayers = new HashSet<>();

    public EntanglingRootsAbility(ElementSmp plugin) {
        super("entangling_roots", 80, 25, 2);
        this.plugin = plugin;
    }

    public static boolean isEntangled(UUID uuid) {
        return entangledPlayers.contains(uuid);
    }

    @Override
    public boolean execute(ElementContext context) {
        Player player = context.getPlayer();
        double range = 10;
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
        if (target instanceof Player targetPlayer) {
            targetPlayer.sendMessage(ChatColor.RED + "You have been pulled into the ground by roots!");
            entangledPlayers.add(targetPlayer.getUniqueId());
        }

        Location targetLoc = target.getLocation();
        targetLoc.getWorld().playSound(targetLoc, Sound.BLOCK_ROOTS_BREAK, 1.5f, 0.5f);
        targetLoc.getWorld().spawnParticle(Particle.BLOCK, targetLoc, 50, 0.5, 0.5, 0.5, 0.1, org.bukkit.Material.DIRT.createBlockData());

        // Pull them slightly down
        Location pullDownLoc = targetLoc.clone().add(0, -1.0, 0);
        target.teleport(pullDownLoc);
        target.setVelocity(new Vector(0, 0, 0));

        // Apply slowness and jump boost (level 250 prevents jumping)
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 5, false, false, true));
        target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 100, 250, false, false, false));

        final LivingEntity finalTarget = target;

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (!finalTarget.isValid() || (finalTarget instanceof Player p && !p.isOnline()) || ticks >= 100) { // 5 seconds
                    // Pull them back up if they are still alive
                    if (finalTarget.isValid()) {
                        if (!(finalTarget instanceof Player p) || p.isOnline()) {
                            finalTarget.teleport(finalTarget.getLocation().add(0, 1.0, 0));
                            finalTarget.removePotionEffect(PotionEffectType.SLOWNESS);
                            finalTarget.removePotionEffect(PotionEffectType.JUMP_BOOST);
                        }
                    }
                    if (finalTarget instanceof Player targetPlayer) {
                        entangledPlayers.remove(targetPlayer.getUniqueId());
                    }
                    cancel();
                    return;
                }

                // Keep them at the pull down location and apply suffocation damage
                finalTarget.teleport(pullDownLoc);
                finalTarget.setVelocity(new Vector(0, 0, 0));
                
                if (ticks % 10 == 0) {
                    finalTarget.damage(1.0); // 0.5 heart damage per half second
                }
                
                // Visual effects
                if (ticks % 5 == 0) {
                    targetLoc.getWorld().spawnParticle(Particle.BLOCK, finalTarget.getLocation().add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0.05, org.bukkit.Material.MOSS_BLOCK.createBlockData());
                }
                
                ticks += 2;
            }
        }.runTaskTimer(plugin, 0L, 2L); // Run every 2 ticks for much tighter control

        return true;
    }

    @Override
    public String getName() {
        return ChatColor.GREEN + "Entangling Roots";
    }

    @Override
    public String getDescription() {
        return "Pull an entity into the ground and suffocate them for 5 seconds.";
    }
}
