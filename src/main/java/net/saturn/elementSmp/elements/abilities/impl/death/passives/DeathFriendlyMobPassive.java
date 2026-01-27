package net.saturn.elementSmp.elements.abilities.impl.death.passives;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.config.Constants;
import net.saturn.elementSmp.config.MetadataKeys;
import net.saturn.elementSmp.elements.ElementType;
import net.saturn.elementSmp.managers.ElementManager;
import net.saturn.elementSmp.managers.TrustManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class DeathFriendlyMobPassive implements Listener {
    private final ElementSmp plugin;
    private final TrustManager trustManager;
    private final ElementManager elementManager;

    public DeathFriendlyMobPassive(ElementSmp plugin, TrustManager trustManager, ElementManager elementManager) {
        this.plugin = plugin;
        this.trustManager = trustManager;
        this.elementManager = elementManager;
        startFollowTask();
    }


    private void startFollowTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (org.bukkit.World world : Bukkit.getWorlds()) {
                    for (Mob mob : world.getEntitiesByClass(Mob.class)) {
                        try {
                            if (!mob.hasMetadata(MetadataKeys.Death.SUMMONED_OWNER) || !mob.hasMetadata(MetadataKeys.Death.SUMMONED_UNTIL)) {
                                continue;
                            }

                            long until = mob.getMetadata(MetadataKeys.Death.SUMMONED_UNTIL).get(0).asLong();
                            if (System.currentTimeMillis() > until) {
                                mob.removeMetadata(MetadataKeys.Death.SUMMONED_OWNER, plugin);
                                mob.removeMetadata(MetadataKeys.Death.SUMMONED_UNTIL, plugin);
                                continue;
                            }

                            String ownerStr = mob.getMetadata(MetadataKeys.Death.SUMMONED_OWNER).get(0).asString();
                            UUID ownerId = UUID.fromString(ownerStr);
                            Player owner = Bukkit.getPlayer(ownerId);

                            if (owner != null && owner.isOnline()) {
                                // NEW: Make the mob swim if it's in water
                                if (mob.getLocation().getBlock().isLiquid()) {
                                    mob.setVelocity(mob.getVelocity().add(new org.bukkit.util.Vector(0, 0.1, 0)));
                                }

                                // Handle cross-world following
                                if (!mob.getWorld().equals(owner.getWorld())) {
                                    mob.teleport(owner.getLocation());
                                    continue;
                                }

                                double distance = mob.getLocation().distance(owner.getLocation());

                                // If too far, teleport closer (but ONLY if owner is on ground)
                                if (distance > Constants.Distance.MOB_TELEPORT_DISTANCE) {
                                    if (owner.isOnGround()) {
                                        mob.teleport(owner.getLocation());
                                    }
                                    continue;
                                }

                                // If mob has no target or target is dead/invalid, look for enemies
                                LivingEntity currentTarget = mob.getTarget();
                                if (currentTarget == null || !currentTarget.isValid() || currentTarget.isDead()) {
                                    // Look for nearest enemy to attack
                                    Player nearestEnemy = null;
                                    double bestDistance = Double.MAX_VALUE;

                                    for (Player player : mob.getWorld().getPlayers()) {
                                        if (player.equals(owner)) continue;
                                        if (trustManager.isTrusted(owner.getUniqueId(), player.getUniqueId())) continue;

                                        double playerDistance = mob.getLocation().distanceSquared(player.getLocation());
                                        if (playerDistance < bestDistance && playerDistance < Constants.Distance.MOB_ATTACK_RADIUS * Constants.Distance.MOB_ATTACK_RADIUS) {
                                            bestDistance = playerDistance;
                                            nearestEnemy = player;
                                        }
                                    }

                                    if (nearestEnemy != null) {
                                        mob.setTarget(nearestEnemy);
                                        mob.setAware(true);
                                    } else if (distance > Constants.Distance.MOB_FOLLOW_DISTANCE) {
                                        // No enemies, follow owner
                                        mob.getPathfinder().moveTo(owner.getLocation(), 1.2);
                                    }
                                }
                            }
                        } catch (Exception ignored) {}
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMobTarget(EntityTargetLivingEntityEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;
        if (event.getTarget() == null) return;

        if (mob.hasMetadata(MetadataKeys.Death.SUMMONED_OWNER)) {
            String ownerStr = mob.getMetadata(MetadataKeys.Death.SUMMONED_OWNER).get(0).asString();
            UUID ownerId = UUID.fromString(ownerStr);

            // Don't target owner or trusted players
            if (event.getTarget().getUniqueId().equals(ownerId) || (event.getTarget() instanceof Player targetPlayer && trustManager.isTrusted(ownerId, targetPlayer.getUniqueId()))) {
                event.setCancelled(true);
                event.setTarget(null);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onOwnerDamage(EntityDamageByEntityEvent event) {
        Player attacker = null;
        if (event.getDamager() instanceof Player p) {
            attacker = p;
        } else if (event.getDamager() instanceof Projectile proj && proj.getShooter() instanceof Player p) {
            attacker = p;
        }

        if (attacker == null) return;

        final Player finalAttacker = attacker;
        final LivingEntity victim = (LivingEntity) event.getEntity();

        // If a player attacks someone, make their summoned mobs attack that person
        for (Mob mob : victim.getWorld().getEntitiesByClass(Mob.class)) {
            if (mob.hasMetadata(MetadataKeys.Death.SUMMONED_OWNER)) {
                String ownerStr = mob.getMetadata(MetadataKeys.Death.SUMMONED_OWNER).get(0).asString();
                UUID ownerId = UUID.fromString(ownerStr);

                if (victim.getUniqueId().equals(ownerId)) {
                    // Owner was attacked, target the attacker
                    mob.setTarget(finalAttacker);
                } else if (finalAttacker.getUniqueId().equals(ownerId)) {
                    // Owner attacked someone, target the victim
                    if (!(victim instanceof Player targetPlayer && trustManager.isTrusted(ownerId, targetPlayer.getUniqueId()))) {
                        mob.setTarget(victim);
                    }
                }
            }
        }
    }
}
