package net.saturn.elementSmp.elements.impl.earth.listeners;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.config.MetadataKeys;
import net.saturn.elementSmp.managers.TrustManager;
import net.saturn.elementSmp.util.bukkit.MetadataHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class EarthFriendlyMobListener implements Listener {
    private final ElementSmp plugin;
    private final TrustManager trustManager;

    public EarthFriendlyMobListener(ElementSmp plugin, TrustManager trustManager) {
        this.plugin = plugin;
        this.trustManager = trustManager;
        startFollowTask();
    }

    private void startFollowTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (org.bukkit.World world : Bukkit.getWorlds()) {
                    for (Mob mob : world.getEntitiesByClass(Mob.class)) {
                        try {
                            if (!mob.hasMetadata(MetadataKeys.Earth.CHARMED_OWNER) || !mob.hasMetadata(MetadataKeys.Earth.CHARMED_UNTIL)) {
                                continue;
                            }

                            long until = mob.getMetadata(MetadataKeys.Earth.CHARMED_UNTIL).get(0).asLong();
                            if (System.currentTimeMillis() > until) {
                                mob.removeMetadata(MetadataKeys.Earth.CHARMED_OWNER, plugin);
                                mob.removeMetadata(MetadataKeys.Earth.CHARMED_UNTIL, plugin);
                                continue;
                            }

                            String ownerStr = mob.getMetadata(MetadataKeys.Earth.CHARMED_OWNER).get(0).asString();
                            UUID ownerId = UUID.fromString(ownerStr);
                            Player owner = Bukkit.getPlayer(ownerId);

                            if (owner != null && owner.isOnline()) {
                                double distance = mob.getLocation().distance(owner.getLocation());

                                // If too far, teleport closer
                                if (distance > 30) {
                                    mob.teleport(owner.getLocation());
                                } else {
                                    // Look for enemies to attack first
                                    Player nearestEnemy = null;
                                    double bestDistance = Double.MAX_VALUE;

                                    for (Player player : mob.getWorld().getPlayers()) {
                                        if (player.equals(owner)) continue;
                                        if (trustManager.isTrusted(owner.getUniqueId(), player.getUniqueId())) continue;

                                        double playerDistance = mob.getLocation().distanceSquared(player.getLocation());
                                        if (playerDistance < bestDistance && playerDistance < 16 * 16) { // 16 block range
                                            bestDistance = playerDistance;
                                            nearestEnemy = player;
                                        }
                                    }

                                    if (nearestEnemy != null) {
                                        // Attack the nearest enemy
                                        mob.setTarget(nearestEnemy);
                                    } else if (distance > 3) {
                                        // No enemies nearby, follow the owner
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

    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
    public void onTarget(EntityTargetLivingEntityEvent e) {
        // Earth charmed mobs: don't target owner or trusted players; retarget nearest other player
        if (e.getEntity() instanceof Mob mob && e.getTarget() instanceof Player tgt) {
            if (mob.hasMetadata(MetadataKeys.Earth.CHARMED_OWNER) && mob.hasMetadata(MetadataKeys.Earth.CHARMED_UNTIL)) {
                try {
                    String ownerStr = mob.getMetadata(MetadataKeys.Earth.CHARMED_OWNER).get(0).asString();
                    long until = mob.getMetadata(MetadataKeys.Earth.CHARMED_UNTIL).get(0).asLong();
                    UUID ownerId = UUID.fromString(ownerStr);

                    if (System.currentTimeMillis() > until) return; // expired

                    Player owner = Bukkit.getPlayer(ownerId);
                    if (owner == null) return;

                    if (tgt.equals(owner) || trustManager.isTrusted(ownerId, tgt.getUniqueId())) {
                        e.setCancelled(true);
                    }
                } catch (Exception ignored) {}
            }
        }
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.HIGH)
    public void onDamage(EntityDamageByEntityEvent e) {
        // Owner or trusted player shouldn't hurt the mob
        if (e.getEntity() instanceof Mob mob && e.getDamager() instanceof Player damager) {
            if (mob.hasMetadata(MetadataKeys.Earth.CHARMED_OWNER)) {
                try {
                    String ownerStr = mob.getMetadata(MetadataKeys.Earth.CHARMED_OWNER).get(0).asString();
                    UUID ownerId = UUID.fromString(ownerStr);
                    if (damager.getUniqueId().equals(ownerId) || trustManager.isTrusted(ownerId, damager.getUniqueId())) {
                        e.setCancelled(true);
                    }
                } catch (Exception ignored) {}
            }
        }
    }
}