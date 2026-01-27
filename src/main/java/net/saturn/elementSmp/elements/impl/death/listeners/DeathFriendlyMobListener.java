package net.saturn.elementSmp.elements.impl.death.listeners;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.config.MetadataKeys;
import net.saturn.elementSmp.util.bukkit.MetadataHelper;
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

public class DeathFriendlyMobListener implements Listener {
    private final ElementSmp plugin;
    private final TrustManager trustManager;
    private final ElementManager elementManager;

    public DeathFriendlyMobListener(ElementSmp plugin, TrustManager trustManager, ElementManager elementManager) {
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
                                if (distance > 30) {
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
                                        if (playerDistance < bestDistance && playerDistance < 20 * 20) {
                                            bestDistance = playerDistance;
                                            nearestEnemy = player;
                                        }
                                    }

                                    if (nearestEnemy != null) {
                                        mob.setTarget(nearestEnemy);
                                        mob.setAware(true);
                                    } else if (distance > 3) {
                                        // No enemies, follow owner
                                        mob.setTarget(null);
                                        mob.getPathfinder().moveTo(owner.getLocation(), 1.2);
                                    }
                                }
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 10L); // Run every 0.5 seconds
    }

    @EventHandler
    public void onDeath(org.bukkit.event.entity.EntityDeathEvent event) {
        if (event.getEntity().hasMetadata(MetadataKeys.Death.SUMMONED_OWNER)) {
            event.getDrops().clear();
            event.setDroppedExp(0);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTarget(EntityTargetLivingEntityEvent e) {
        // Death summoned undead: don't target owner or trusted players
        if (e.getEntity() instanceof Mob mob && e.getTarget() instanceof Player target) {
            if (mob.hasMetadata(MetadataKeys.Death.SUMMONED_OWNER) && mob.hasMetadata(MetadataKeys.Death.SUMMONED_UNTIL)) {
                try {
                    String ownerStr = mob.getMetadata(MetadataKeys.Death.SUMMONED_OWNER).get(0).asString();
                    long until = mob.getMetadata(MetadataKeys.Death.SUMMONED_UNTIL).get(0).asLong();
                    UUID ownerId = UUID.fromString(ownerStr);

                    if (System.currentTimeMillis() > until) return; // expired

                    // Don't target owner or trusted players
                    Player owner = Bukkit.getPlayer(ownerId);
                    if (owner != null && (target.equals(owner) || trustManager.isTrusted(owner.getUniqueId(), target.getUniqueId()))) {
                        e.setCancelled(true);

                        // Retarget to nearest enemy instead
                        Player nearestEnemy = null;
                        double bestDistance = Double.MAX_VALUE;
                        for (Player p : mob.getWorld().getPlayers()) {
                            if (p.equals(owner) || trustManager.isTrusted(owner.getUniqueId(), p.getUniqueId())) continue;
                            double d = p.getLocation().distanceSquared(mob.getLocation());
                            if (d < bestDistance && d < 20*20) {
                                bestDistance = d;
                                nearestEnemy = p;
                            }
                        }
                        if (nearestEnemy != null) {
                            e.setTarget(nearestEnemy);
                        }
                        return;
                    }
                } catch (Exception ignored) {}
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(EntityDamageByEntityEvent e) {
        // Prevent death summoned undead from damaging their owner or trusted players
        if (e.getDamager() instanceof Mob mob && e.getEntity() instanceof Player target) {
            if (mob.hasMetadata(MetadataKeys.Death.SUMMONED_OWNER) && mob.hasMetadata(MetadataKeys.Death.SUMMONED_UNTIL)) {
                try {
                    String ownerStr = mob.getMetadata(MetadataKeys.Death.SUMMONED_OWNER).get(0).asString();
                    long until = mob.getMetadata(MetadataKeys.Death.SUMMONED_UNTIL).get(0).asLong();
                    UUID ownerId = UUID.fromString(ownerStr);

                    if (System.currentTimeMillis() > until) return; // expired

                    // Don't damage owner or trusted players
                    Player owner = Bukkit.getPlayer(ownerId);
                    if (owner != null && (target.equals(owner) || trustManager.isTrusted(owner.getUniqueId(), target.getUniqueId()))) {
                        e.setCancelled(true);
                        return;
                    }
                } catch (Exception ignored) {}
            }
        }

        // Prevent summoned mobs from attacking each other if they have the same owner
        if (e.getDamager() instanceof Mob damager && e.getEntity() instanceof Mob victim) {
            if (damager.hasMetadata(MetadataKeys.Death.SUMMONED_OWNER) && victim.hasMetadata(MetadataKeys.Death.SUMMONED_OWNER)) {
                try {
                    String damagerOwner = damager.getMetadata(MetadataKeys.Death.SUMMONED_OWNER).get(0).asString();
                    String victimOwner = victim.getMetadata(MetadataKeys.Death.SUMMONED_OWNER).get(0).asString();
                    if (damagerOwner.equals(victimOwner)) {
                        e.setCancelled(true);
                        return;
                    }
                } catch (Exception ignored) {}
            }
        }

        // NEW FEATURE: When Death element player hits something, make ALL their summoned mobs attack it
        if (e.getDamager() instanceof Player attacker && e.getEntity() instanceof LivingEntity target) {
            if (target.equals(attacker)) return;
            if (target instanceof Player targetPlayer) {
                if (trustManager.isTrusted(attacker.getUniqueId(), targetPlayer.getUniqueId())) return;
            }

            // Check if attacker has Death element
            if (elementManager.getPlayerElement(attacker) != ElementType.DEATH) {
                return;
            }

            // Find all summoned mobs belonging to this player
            for (Mob mob : attacker.getWorld().getEntitiesByClass(Mob.class)) {
                if (!mob.hasMetadata(MetadataKeys.Death.SUMMONED_OWNER) || !mob.hasMetadata(MetadataKeys.Death.SUMMONED_UNTIL)) {
                    continue;
                }

                try {
                    String ownerStr = mob.getMetadata(MetadataKeys.Death.SUMMONED_OWNER).get(0).asString();
                    long until = mob.getMetadata(MetadataKeys.Death.SUMMONED_UNTIL).get(0).asLong();
                    UUID ownerId = UUID.fromString(ownerStr);

                    // Check if this mob belongs to the attacker
                    if (ownerId.equals(attacker.getUniqueId()) && System.currentTimeMillis() <= until) {
                        // Make the mob attack what the player attacked
                        mob.setTarget(target);
                        mob.setAware(true);
                    }
                } catch (Exception ignored) {}
            }
        }
    }
}
