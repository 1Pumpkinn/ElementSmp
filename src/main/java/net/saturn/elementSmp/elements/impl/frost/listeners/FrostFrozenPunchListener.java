package net.saturn.elementSmp.elements.impl.frost.listeners;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.elements.ElementType;
import net.saturn.elementSmp.elements.abilities.impl.frost.FrostPunchAbility;
import net.saturn.elementSmp.managers.ElementManager;
import net.saturn.elementSmp.managers.TrustManager;
import io.papermc.paper.event.entity.EntityMoveEvent;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class FrostFrozenPunchListener implements Listener {

    private final ElementSmp plugin;
    private final ElementManager elementManager;
    private final TrustManager trustManager;

    public static final String META_FROZEN = "frost_frozen";

    public FrostFrozenPunchListener(ElementSmp plugin, ElementManager elementManager, TrustManager trustManager) {
        this.plugin = plugin;
        this.elementManager = elementManager;
        this.trustManager = trustManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof LivingEntity victim)) return;


        // Check if attacker has Frost element
        if (elementManager.getPlayerElement(attacker) != ElementType.FROST) {
            return;
        }

        // Check if frozen punch metadata exists
        if (!attacker.hasMetadata(FrostPunchAbility.META_FROZEN_PUNCH_READY)) {
            return;
        }

        long until = attacker.getMetadata(FrostPunchAbility.META_FROZEN_PUNCH_READY).get(0).asLong();
        if (System.currentTimeMillis() > until) {
            attacker.removeMetadata(FrostPunchAbility.META_FROZEN_PUNCH_READY, plugin);
            return;
        }

        // Don't freeze trusted players or self
        if (attacker.equals(victim) || (victim instanceof Player targetPlayer && trustManager.isTrusted(attacker.getUniqueId(), targetPlayer.getUniqueId()))) {
            return;
        }

        // Remove the ready state (consume ability)
        attacker.removeMetadata(FrostPunchAbility.META_FROZEN_PUNCH_READY, plugin);

        // Apply freeze effect
        applyFreezeEffect(victim);

        // Feedback
        Location hitLoc = victim.getEyeLocation();
        victim.getWorld().spawnParticle(Particle.SNOWFLAKE, hitLoc, 50, 0.3, 0.5, 0.3, 0.1, null, true);
        victim.getWorld().spawnParticle(Particle.CLOUD, hitLoc, 25, 0.3, 0.5, 0.3, 0.05, null, true);
        victim.getWorld().playSound(hitLoc, Sound.BLOCK_GLASS_BREAK, 1.0f, 0.5f);

    }

    private void applyFreezeEffect(LivingEntity entity) {
        long freezeUntil = System.currentTimeMillis() + 5000L; // 5 seconds
        entity.setMetadata(META_FROZEN, new FixedMetadataValue(plugin, freezeUntil));

        entity.setFreezeTicks(entity.getMaxFreezeTicks());
        entity.setVelocity(new Vector(0, 0, 0));

        // Enable flight for players to prevent "kick for flying"
        final boolean wasAllowFlight = (entity instanceof Player p) ? p.getAllowFlight() : false;
        if (entity instanceof Player p) {
            p.setAllowFlight(true);
        }

        if (entity instanceof Mob mob) {
            mob.setAware(true);
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (mob.isValid()) mob.setAware(true);
                }
            }.runTaskLater(plugin, 100L);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!entity.isValid() || entity.isDead()) {
                    cancel();
                    return;
                }

                if (!entity.hasMetadata(META_FROZEN)) {
                    entity.setFreezeTicks(0);
                    if (entity instanceof Player p) {
                        p.setAllowFlight(wasAllowFlight);
                    }
                    cancel();
                    return;
                }

                long until = entity.getMetadata(META_FROZEN).get(0).asLong();
                if (System.currentTimeMillis() > until) {
                    entity.removeMetadata(META_FROZEN, plugin);
                    entity.setFreezeTicks(0);
                    if (entity instanceof Player p) {
                        p.setAllowFlight(wasAllowFlight);
                    }
                    cancel();
                } else {
                    entity.setFreezeTicks(entity.getMaxFreezeTicks());
                    // Keep them in place
                    entity.setVelocity(new Vector(0, 0, 0));
                    
                    // Visual effect
                    entity.getWorld().spawnParticle(Particle.SNOWFLAKE, entity.getLocation().add(0, 1, 0), 3, 0.3, 0.5, 0.3, 0.01, null, true);
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getPlayer().hasMetadata(META_FROZEN)) {
            Location from = event.getFrom();
            Location to = event.getTo();
            if (from.getX() != to.getX() || from.getZ() != to.getZ() || from.getY() != to.getY()) {
                event.setTo(from);
            }
        }
    }

    @EventHandler
    public void onEntityMove(EntityMoveEvent event) {
        if (event.getEntity().hasMetadata(META_FROZEN)) {
            event.setCancelled(true);
        }
    }
}