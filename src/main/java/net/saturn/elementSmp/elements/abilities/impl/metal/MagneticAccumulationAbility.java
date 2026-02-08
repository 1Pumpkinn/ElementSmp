package net.saturn.elementSmp.elements.abilities.impl.metal;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.config.Constants;
import net.saturn.elementSmp.config.MetadataKeys;
import net.saturn.elementSmp.elements.ElementContext;
import net.saturn.elementSmp.elements.abilities.BaseAbility;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Optional;
import java.util.UUID;

import org.bukkit.util.Vector;

public class MagneticAccumulationAbility extends BaseAbility implements Listener {
    private final ElementSmp plugin;

    public static final String META_ACCUM_UNTIL = MetadataKeys.Metal.MAGNETIC_ACCUM_UNTIL;
    public static final String META_ACCUM_OWNER = MetadataKeys.Metal.MAGNETIC_ACCUM_OWNER;
    public static final String META_ACCUM_DAMAGE = MetadataKeys.Metal.MAGNETIC_ACCUM_DAMAGE;

    public MagneticAccumulationAbility(ElementSmp plugin) {
        super();
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean execute(ElementContext context) {
        Player player = context.getPlayer();

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
            player.sendMessage(ChatColor.RED + "You must look at a valid entity to magnetize their damage!");
            return false;
        }

        setActive(player, true);

        long durationMs = Constants.Duration.METAL_CHAIN_STUN_MS; // 3 seconds
        long until = System.currentTimeMillis() + durationMs;

        target.setMetadata(META_ACCUM_UNTIL, new FixedMetadataValue(plugin, until));
        target.setMetadata(META_ACCUM_OWNER, new FixedMetadataValue(plugin, player.getUniqueId().toString()));
        target.setMetadata(META_ACCUM_DAMAGE, new FixedMetadataValue(plugin, 0.0));

        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.2f);
        player.getWorld().spawnParticle(Particle.CRIT, player.getLocation().add(0, 0.9, 0), 15, 0.3, 0.6, 0.3, 0.05, null, true);
        target.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, target.getLocation().add(0, 0.9, 0), 20, 0.3, 0.6, 0.3, 0.05, null, true);

        LivingEntity finalTarget = target;
        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = 60; // 3 seconds

            @Override
            public void run() {
                if (!finalTarget.isValid() || finalTarget.isDead() || ticks >= maxTicks || !player.isOnline()) {
                    if (finalTarget.isValid()) {
                        double accumulated = getAccumulated(finalTarget);
                        if (accumulated > 0.0) {
                            double base = accumulated * 0.3;
                            double finalDamage = Math.min(8.0, base);
                            double newHealth = Math.max(0.0, finalTarget.getHealth() - finalDamage);
                            finalTarget.setHealth(newHealth);
                            finalTarget.getWorld().spawnParticle(Particle.LAVA, finalTarget.getLocation().add(0, 1, 0), 1, 0, 0, 0, 0.1, null, true);
                            finalTarget.getWorld().playSound(finalTarget.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.2f);
                        }
                        clearMetadata(finalTarget);
                    }
                    if (player.isOnline()) {
                        setActive(player, false);
                    }
                    cancel();
                    return;
                }

                drawParticleBeam(player, finalTarget);

                if (ticks % 20 == 0) {
                    player.getWorld().playSound(player.getLocation(), Sound.BLOCK_CONDUIT_AMBIENT_SHORT, 0.5f, 1.5f);
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        return true;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity victim)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;

        if (!victim.hasMetadata(META_ACCUM_UNTIL) || !victim.hasMetadata(META_ACCUM_OWNER) || !victim.hasMetadata(META_ACCUM_DAMAGE)) return;

        long until = getLongMeta(victim, META_ACCUM_UNTIL).orElse(0L);
        if (System.currentTimeMillis() > until) {
            clearMetadata(victim);
            return;
        }

        String ownerStr = getStringMeta(victim, META_ACCUM_OWNER).orElse("");
        if (!ownerStr.equals(attacker.getUniqueId().toString())) return;

        double cooldown = attacker.getAttackCooldown();
        double damageToAdd = event.getFinalDamage();

        if (cooldown < 1.0) {
            damageToAdd *= 0.1;
        }

        double current = getAccumulated(victim);
        double updated = current + Math.max(0.0, damageToAdd);

        victim.setMetadata(META_ACCUM_DAMAGE, new FixedMetadataValue(plugin, updated));

        victim.getWorld().spawnParticle(Particle.CRIT, victim.getLocation().add(0, 0.9, 0), 10, 0.3, 0.6, 0.3, 0.05, null, true);
        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 2.0f);
        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.0f, 2.0f);

        event.setCancelled(true);
    }

    private void drawParticleBeam(Player player, LivingEntity target) {
        Vector start = player.getLocation().toVector().add(new Vector(0, 1, 0));
        Vector end = target.getLocation().toVector().add(new Vector(0, 1, 0));
        Vector direction = end.clone().subtract(start).normalize();
        double distance = start.distance(end);

        for (double d = 0; d < distance; d += 0.5) {
            Vector point = start.clone().add(direction.clone().multiply(d));

            double offsetX = (Math.random() - 0.5) * 0.4;
            double offsetY = (Math.random() - 0.5) * 0.4;
            double offsetZ = (Math.random() - 0.5) * 0.4;

            point.add(new Vector(offsetX, offsetY, offsetZ));

            player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, point.getX(), point.getY(), point.getZ(), 1, 0, 0, 0, 0, null, true);
        }
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_CONDUIT_AMBIENT_SHORT, 0.5f, 1.5f);
    }

    private double getAccumulated(LivingEntity entity) {
        if (!entity.hasMetadata(META_ACCUM_DAMAGE)) return 0.0;
        try {
            Object val = entity.getMetadata(META_ACCUM_DAMAGE).get(0).value();
            if (val instanceof Double d) return d;
            if (val instanceof Number n) return n.doubleValue();
        } catch (Exception ignored) {}
        return 0.0;
    }

    private Optional<Long> getLongMeta(LivingEntity entity, String key) {
        try {
            Object val = entity.getMetadata(key).get(0).value();
            if (val instanceof Long l) return Optional.of(l);
            if (val instanceof Number n) return Optional.of(n.longValue());
        } catch (Exception ignored) {}
        return Optional.empty();
    }

    private Optional<String> getStringMeta(LivingEntity entity, String key) {
        try {
            Object val = entity.getMetadata(key).get(0).value();
            if (val instanceof String s) return Optional.of(s);
        } catch (Exception ignored) {}
        return Optional.empty();
    }

    private void clearMetadata(LivingEntity entity) {
        entity.removeMetadata(META_ACCUM_UNTIL, plugin);
        entity.removeMetadata(META_ACCUM_OWNER, plugin);
        entity.removeMetadata(META_ACCUM_DAMAGE, plugin);
    }
}
