package net.saturn.elementSmp.elements.abilities.impl.metal;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.elements.ElementContext;
import net.saturn.elementSmp.elements.abilities.BaseAbility;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class MetalDashAbility extends BaseAbility implements Listener {
    private final ElementSmp plugin;
    private final Set<UUID> stunnedPlayers = new HashSet<>();
    private final Set<UUID> dashingPlayers = new HashSet<>();

    public MetalDashAbility(ElementSmp plugin) {
        super("metal_dash", 50, 10, 1);
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean execute(ElementContext context) {
        Player player = context.getPlayer();
        Vector direction = player.getLocation().getDirection().normalize();

        // Apply velocity boost - allow downward dashing if looking down
        Vector dashVelocity = direction.multiply(2.5);
        
        // Only force an upward boost if the player is NOT looking down
        if (direction.getY() > -0.2) {
            dashVelocity.setY(Math.max(dashVelocity.getY(), 0.4));
        }
        
        player.setVelocity(dashVelocity);

        // Track damaged entities to prevent multiple hits
        Set<UUID> damagedEntities = new HashSet<>();

        // Play sound
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 1.0f, 1.5f);

        setActive(player, true);
        dashingPlayers.add(player.getUniqueId());

        new BukkitRunnable() {
            int ticks = 0;
            final int maxMovementTicks = 40; // Dash movement phase
            Integer landedAtTick = null;
            final int graceTicks = 30; // 1.5 seconds grace after landing

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cleanup();
                    cancel();
                    return;
                }

                Location loc = player.getLocation();

                // 1. Particle effects
                if (ticks < maxMovementTicks || landedAtTick != null) {
                    player.getWorld().spawnParticle(Particle.CRIT, loc, 8, 0.3, 0.3, 0.3, 0.1, null, true);
                }

                // 2. Check for nearby entities (always active until hit or stun)
                if (ticks % 2 == 0) {
                    for (LivingEntity entity : loc.getNearbyLivingEntities(2.5)) {
                        if (entity.equals(player)) continue;
                        if (damagedEntities.contains(entity.getUniqueId())) continue;
                        if (entity instanceof org.bukkit.entity.ArmorStand) continue;
                        if (entity instanceof Player targetPlayer) {
                            if (context.getTrustManager().isTrusted(player.getUniqueId(), targetPlayer.getUniqueId())) continue;
                        }

                        // HIT SUCCESSFUL - Now allows hitting multiple entities
                        damagedEntities.add(entity.getUniqueId());

                        // Deal TRUE DAMAGE
                        entity.setHealth(Math.max(0, entity.getHealth() - 4.0));

                        // Effects
                        Vector knockback = entity.getLocation().toVector().subtract(loc.toVector()).normalize();
                        knockback.setY(0.3);
                        entity.setVelocity(knockback.multiply(0.5));
                        entity.getWorld().spawnParticle(Particle.CRIT, entity.getLocation(), 15, 0.5, 0.5, 0.5, 0.1, null, true);
                        entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.2f);
                    }
                }

                // 3. Landing detection
                // Ignore the first 5 ticks to allow the player to leave the ground if they started there
                if (landedAtTick == null && ticks > 5 && player.isOnGround()) {
                    landedAtTick = ticks;
                }

                // 4. Termination logic
                if (landedAtTick != null) {
                    // Player has landed, check grace period
                    if (ticks - landedAtTick >= graceTicks) {
                        // Only stun if NO entities were hit throughout the dash
                        if (damagedEntities.isEmpty()) {
                            applyStun(player);
                        }
                        cleanup();
                        cancel();
                        return;
                    }
                }

                ticks++;

                // Safety timeout (30 seconds) - No stun if still in air
                if (ticks > 600) {
                    cleanup();
                    cancel();
                }
            }

            private void cleanup() {
                dashingPlayers.remove(player.getUniqueId());
                setActive(player, false);
            }
        }.runTaskTimer(plugin, 0L, 1L);

        return true;
    }

    private void applyStun(Player player) {
        // Add player to stunned set
        stunnedPlayers.add(player.getUniqueId());

        // Apply potion effects for visual/mechanical feedback
        player.addPotionEffect(new org.bukkit.potion.PotionEffect(PotionEffectType.SLOWNESS, 100, 4, true, false, true));
        player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.WEAKNESS, 100, 4, true, false, true));

        // Enable flight to prevent "kick for flying" while suspended in air
        boolean wasAllowFlight = player.getAllowFlight();
        player.setAllowFlight(true);

        // Visual and audio feedback for the stun
        player.getWorld().spawnParticle(Particle.SMOKE, player.getLocation().add(0, 1, 0), 30, 0.3, 0.5, 0.3, 0.05, null, true);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_DAMAGE, 1.0f, 0.8f);

        // Remove from stunned set after 5 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                stunnedPlayers.remove(player.getUniqueId());
                
                // Restore flight state if player is still online
                if (player.isOnline()) {
                    // Only disable if they weren't allowed to fly before and aren't in creative/spectator
                    if (!wasAllowFlight && player.getGameMode() != org.bukkit.GameMode.CREATIVE && player.getGameMode() != org.bukkit.GameMode.SPECTATOR) {
                        player.setAllowFlight(false);
                    }
                }
            }
        }.runTaskLater(plugin, 100L);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // Check if player is stunned
        if (stunnedPlayers.contains(playerId)) {
            Location from = event.getFrom();
            Location to = event.getTo();

            // If player tried to move (not just looking around)
            if (to != null && (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ())) {
                // Cancel the movement by teleporting back
                event.setTo(from);

                // Optional: Small visual feedback
                if (player.getTicksLived() % 10 == 0) { // Only every 10 ticks to avoid spam
                }
            }
        }
    }

    @Override
    public String getName() {
        return ChatColor.GRAY + "Metal Dash";
    }

    @Override
    public String getDescription() {
        return "Dash forward, hitting all enemies in your path. You cannot be stunned in the air; once you land, you have 1.5s to hit at least one entity to avoid a 5s stun. (50 mana)";
    }
}
