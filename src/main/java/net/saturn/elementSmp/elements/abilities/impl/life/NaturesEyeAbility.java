package net.saturn.elementSmp.elements.abilities.impl.life;

import net.saturn.elementSmp.elements.ElementContext;
import net.saturn.elementSmp.elements.abilities.BaseAbility;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class NaturesEyeAbility extends BaseAbility {

    private final net.saturn.elementSmp.ElementSmp plugin;

    public NaturesEyeAbility(net.saturn.elementSmp.ElementSmp plugin) {
        super();
        this.plugin = plugin;
    }

    @Override
    public boolean execute(ElementContext context) {
        Player player = context.getPlayer();
        int radius = 25;
        int duration = 5 * 20; // 5 seconds in ticks

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ALLAY_AMBIENT_WITH_ITEM, 1.0f, 1.0f);
        player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, player.getEyeLocation(), 30, 0.5, 0.5, 0.5, 0.1);

        // Get Main Scoreboard and Team
        Scoreboard mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team natureTeam = mainScoreboard.getTeam("NatureEyeGlobal");
        if (natureTeam == null) {
            natureTeam = mainScoreboard.registerNewTeam("NatureEyeGlobal");
        }
        natureTeam.setColor(ChatColor.GREEN);

        // Track entities we made glow
        Set<UUID> glowingEntities = new HashSet<>();

        // Capture for lambda
        Team finalNatureTeam = natureTeam;

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= duration || !player.isOnline()) {
                    // Cleanup
                    cleanup(mainScoreboard, glowingEntities);
                    this.cancel();
                    return;
                }

                // Scan for nearby entities
                List<Entity> nearby = player.getNearbyEntities(radius, radius, radius);
                Set<UUID> currentNearbyUUIDs = new HashSet<>();

                for (Entity entity : nearby) {
                    if (entity instanceof LivingEntity livingEntity && !entity.equals(player)) {
                        UUID uuid = entity.getUniqueId();
                        currentNearbyUUIDs.add(uuid);

                        // Make them glow if not already handled
                        if (!glowingEntities.contains(uuid)) {
                            livingEntity.setGlowing(true);
                            String entry = entity instanceof Player ? entity.getName() : uuid.toString();
                            if (!finalNatureTeam.hasEntry(entry)) {
                                finalNatureTeam.addEntry(entry);
                            }
                            glowingEntities.add(uuid);
                        }

                        // Use particles to highlight entity (additional visual aid)
                        player.spawnParticle(
                            Particle.HAPPY_VILLAGER, 
                            livingEntity.getEyeLocation().add(0, 0.5, 0), 
                            1, 0.2, 0.2, 0.2, 0
                        );

                        // Tracking sprinting for players
                        if (livingEntity instanceof Player targetPlayer) {
                            if (targetPlayer.isSprinting()) {
                                player.spawnParticle(
                                        Particle.FALLING_SPORE_BLOSSOM,
                                        targetPlayer.getLocation().add(0, 1.5, 0),
                                        5, 0.3, 0.5, 0.3, 0.05
                                );
                                
                                if (ticks % 10 == 0) {
                                    player.playSound(targetPlayer.getLocation(), Sound.BLOCK_MOSS_STEP, 0.6f, 1.2f);
                                }
                            }
                        }
                    }
                }

                // Restore entities that are no longer nearby
                // We use a safe copy or iterator removal
                glowingEntities.removeIf(uuid -> {
                    if (!currentNearbyUUIDs.contains(uuid)) {
                        Entity entity = Bukkit.getEntity(uuid);
                        if (entity instanceof LivingEntity livingEntity) {
                            livingEntity.setGlowing(false);
                            String entry = entity instanceof Player ? entity.getName() : uuid.toString();
                            finalNatureTeam.removeEntry(entry);
                        }
                        return true;
                    }
                    return false;
                });

                ticks += 2;
            }
        }.runTaskTimer(plugin, 0L, 2L);

        player.sendMessage(ChatColor.GREEN + "Nature's Eye has revealed nearby life forms!");
        return true;
    }

    private void cleanup(Scoreboard scoreboard, Set<UUID> glowingEntities) {
        Team natureTeam = scoreboard.getTeam("NatureEyeGlobal");
        
        for (UUID uuid : glowingEntities) {
            Entity entity = Bukkit.getEntity(uuid);
            if (entity instanceof LivingEntity livingEntity) {
                livingEntity.setGlowing(false);
                if (natureTeam != null) {
                    String entry = entity instanceof Player ? entity.getName() : uuid.toString();
                    natureTeam.removeEntry(entry);
                }
            }
        }
    }
}
