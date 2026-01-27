package net.saturn.elementSmp.elements.abilities.impl.metal;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.config.Constants;
import net.saturn.elementSmp.config.MetadataKeys;
import net.saturn.elementSmp.elements.ElementContext;
import net.saturn.elementSmp.elements.abilities.BaseAbility;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class MetalChainAbility extends BaseAbility {
    private final ElementSmp plugin;

    // Metadata key for stun tracking
    public static final String META_CHAINED_STUN = MetadataKeys.Metal.CHAIN_STUN;

    public MetalChainAbility(ElementSmp plugin) {
        super();
        this.plugin = plugin;
    }

    @Override
    public boolean execute(ElementContext context) {
        Player player = context.getPlayer();

        // --- Target detection (Entity Raycast with Block Bypass) ---
        double range = 20;
        
        // Raytrace for entities specifically (with a small 0.5 buffer for easier targeting)
        org.bukkit.util.RayTraceResult entityHit = player.getWorld().rayTraceEntities(
                player.getEyeLocation(),
                player.getEyeLocation().getDirection(),
                range,
                0.5, // Ray size buffer for easier targeting
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
            // Check for SOLID blocks only
            org.bukkit.util.RayTraceResult blockHit = player.getWorld().rayTraceBlocks(
                    player.getEyeLocation(),
                    player.getEyeLocation().getDirection(),
                    player.getEyeLocation().distance(living.getEyeLocation()),
                    org.bukkit.FluidCollisionMode.NEVER,
                    true // true = IGNORE non-solid blocks (grass, flowers, etc.)
            );

            if (blockHit == null || blockHit.getHitBlock() == null) {
                target = living;
            } else {
                return false;
            }
        }

        if (target == null) {
            player.sendMessage(ChatColor.RED + "You must look at a valid entity to chain it!");
            return false;
        }

        // Play sounds
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_CHAIN_PLACE, 1.0f, 0.8f);
        target.getWorld().playSound(target.getLocation(), Sound.BLOCK_CHAIN_HIT, 1.0f, 1.0f);

        final LivingEntity finalTarget = target;

        // Start chain particle animation and reeling
        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = 40; // 2 seconds of reeling
            final Location playerStartLoc = player.getEyeLocation().subtract(0, 0.3, 0);

            @Override
            public void run() {
                if (!player.isOnline() || !finalTarget.isValid() || ticks >= maxTicks) {
                    cancel();
                    return;
                }

                // Get current positions
                Location currentPlayerLoc = player.getEyeLocation().subtract(0, 0.3, 0);
                Location targetLoc = finalTarget.getEyeLocation();

                // Calculate distance
                double distance = currentPlayerLoc.distance(targetLoc);

                // If target is close enough, stop
                if (distance < 2.0) {
                    // Set velocity to zero to stop movement
                    finalTarget.setVelocity(new Vector(0, 0, 0));

                    if (finalTarget instanceof Mob mob) {
                        mob.setAware(true);
                    }

                    // Set metadata for stun duration
                    long stunDuration = Constants.Duration.METAL_CHAIN_STUN_MS;
                    long stunUntil = System.currentTimeMillis() + stunDuration;
                    finalTarget.setMetadata(META_CHAINED_STUN, new FixedMetadataValue(plugin, stunUntil));

                    // Enable flight for players to prevent "kick for flying"
                    final boolean wasAllowFlight = (finalTarget instanceof Player p) ? p.getAllowFlight() : false;
                    if (finalTarget instanceof Player p) {
                        p.setAllowFlight(true);
                    }

                    // Schedule metadata removal and re-enable AI after stun expires
                    new BukkitRunnable() {
                        int stunTicks = 0;
                        final int maxStunTicks = (int) (Constants.Duration.METAL_CHAIN_STUN_MS / 50);

                        @Override
                        public void run() {
                            if (!finalTarget.isValid() || stunTicks >= maxStunTicks) {
                                if (finalTarget.isValid()) {
                                    finalTarget.removeMetadata(META_CHAINED_STUN, plugin);
                                    if (finalTarget instanceof Mob mob) {
                                        mob.setAware(true);
                                    }
                                    if (finalTarget instanceof Player p) {
                                        if (!wasAllowFlight && p.getGameMode() != org.bukkit.GameMode.CREATIVE && p.getGameMode() != org.bukkit.GameMode.SPECTATOR) {
                                            p.setAllowFlight(false);
                                        }
                                    }
                                }
                                cancel();
                                return;
                            }

                            // Keep wrapping during stun
                            double radius = 0.6;
                            for (int i = 0; i < 2; i++) {
                                double angle = (stunTicks * 0.4 + (i * Math.PI)) % (2 * Math.PI);
                                double x = Math.cos(angle) * radius;
                                double z = Math.sin(angle) * radius;
                                double y = (stunTicks * 0.08 + (i * 0.5)) % 2.0;
                                
                                Location wrapLoc = finalTarget.getLocation().add(x, y, z);
                                finalTarget.getWorld().spawnParticle(Particle.BLOCK, wrapLoc, 1, 
                                        0, 0, 0, 0, 
                                        Material.MUD.createBlockData(), true);
                            }
                            stunTicks++;
                        }
                    }.runTaskTimer(plugin, 0L, 1L);

                    // Visual/audio feedback for stun
                    player.getWorld().playSound(targetLoc, Sound.BLOCK_ANVIL_LAND, 1.0f, 2.0f);
                    player.getWorld().spawnParticle(Particle.BLOCK, targetLoc, 30,
                            0.3, 0.5, 0.3, 0.1,
                            org.bukkit.Material.MUD.createBlockData(), true);

                    cancel();
                    return;
                }

                // Draw particle chain
                Vector direction = targetLoc.toVector().subtract(currentPlayerLoc.toVector()).normalize();
                double particleSpacing = 0.25; // Slightly wider for block particles
                int numParticles = (int) (distance / particleSpacing);

                for (int i = 0; i <= numParticles; i++) {
                    double t = i * particleSpacing;
                    Location particleLoc = currentPlayerLoc.clone().add(direction.clone().multiply(t));

                    player.getWorld().spawnParticle(Particle.BLOCK, particleLoc, 1,
                            0.02, 0.02, 0.02, 0.0,
                            Material.MUD.createBlockData(), true);
                }

                // Wrap around effect on the target
                double radius = 0.6;
                for (int i = 0; i < 2; i++) {
                    double angle = (ticks * 0.5 + (i * Math.PI)) % (2 * Math.PI);
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    double y = (ticks * 0.1 + (i * 0.5)) % 2.0; // Moves up the body
                    
                    Location wrapLoc = finalTarget.getLocation().add(x, y, z);
                    player.getWorld().spawnParticle(Particle.BLOCK, wrapLoc, 1, 
                            0, 0, 0, 0, 
                            Material.MUD.createBlockData(), true);
                }

                // Apply gentle pull force to target
                Vector pullDirection = currentPlayerLoc.toVector().subtract(targetLoc.toVector()).normalize();

                // Auto-jump logic: detect if target is hitting a block while being pulled
                Vector horizontalDir = pullDirection.clone().setY(0);
                if (horizontalDir.lengthSquared() > 0.01) {
                    horizontalDir.normalize();
                    // Check slightly in front of the entity
                    org.bukkit.block.Block frontFoot = finalTarget.getLocation().add(horizontalDir.clone().multiply(0.7)).getBlock();
                    org.bukkit.block.Block frontHead = finalTarget.getEyeLocation().add(horizontalDir.clone().multiply(0.7)).getBlock();

                    if (frontFoot.getType().isSolid() && !frontHead.getType().isSolid()) {
                        // Room to jump over
                        if (finalTarget.isOnGround() || finalTarget.getVelocity().getY() < 0.1) {
                            Vector vel = finalTarget.getVelocity();
                            vel.setY(0.42); // Enough to clear one block
                            finalTarget.setVelocity(vel);
                        }
                    }
                }

                // Gentle upward component to prevent dragging on ground
                pullDirection.setY(pullDirection.getY() + 0.1);

                // Smooth pull force (not instant velocity)
                double pullStrength = 0.25; // Gentle pull
                Vector currentVelocity = finalTarget.getVelocity();
                Vector newVelocity = currentVelocity.add(pullDirection.multiply(pullStrength));

                // Cap velocity to prevent launching
                double maxSpeed = 0.8;
                if (newVelocity.length() > maxSpeed) {
                    newVelocity = newVelocity.normalize().multiply(maxSpeed);
                }

                finalTarget.setVelocity(newVelocity);

                // Play chain sound periodically
                if (ticks % 10 == 0) {
                    player.getWorld().playSound(currentPlayerLoc, Sound.BLOCK_CHAIN_STEP, 0.5f, 1.2f);
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        return true;
    }

    /**
     * Check if there's a clear chain path between two locations
     * Allows chain to pass through grass, flowers, vines, etc.
     */
    private boolean hasChainPath(Location from, Location to) {
        org.bukkit.util.Vector direction = to.toVector().subtract(from.toVector());
        double distance = direction.length();
        direction.normalize();

        // Check every 0.5 blocks along the path
        for (double d = 0; d < distance; d += 0.5) {
            Location checkLoc = from.clone().add(direction.clone().multiply(d));
            org.bukkit.block.Block block = checkLoc.getBlock();

            // If we hit a non-passable block, chain is blocked
            if (!isPassableBlock(block)) {
                return false;
            }
        }

        return true;
    }

    private static final java.util.Set<Material> PASSABLE_BLOCKS = java.util.EnumSet.of(
            Material.SHORT_GRASS, Material.TALL_GRASS, Material.FERN, Material.LARGE_FERN,
            Material.DEAD_BUSH, Material.DANDELION, Material.POPPY, Material.BLUE_ORCHID,
            Material.ALLIUM, Material.AZURE_BLUET, Material.RED_TULIP, Material.ORANGE_TULIP,
            Material.WHITE_TULIP, Material.PINK_TULIP, Material.OXEYE_DAISY, Material.CORNFLOWER,
            Material.LILY_OF_THE_VALLEY, Material.WITHER_ROSE, Material.SUNFLOWER, Material.LILAC,
            Material.ROSE_BUSH, Material.PEONY, Material.SUGAR_CANE, Material.BAMBOO,
            Material.BAMBOO_SAPLING, Material.WHEAT, Material.CARROTS, Material.POTATOES,
            Material.BEETROOTS, Material.MELON_STEM, Material.PUMPKIN_STEM, Material.ATTACHED_MELON_STEM,
            Material.ATTACHED_PUMPKIN_STEM, Material.SWEET_BERRY_BUSH, Material.CAVE_VINES,
            Material.CAVE_VINES_PLANT, Material.GLOW_BERRIES, Material.VINE, Material.GLOW_LICHEN,
            Material.HANGING_ROOTS, Material.SPORE_BLOSSOM, Material.MOSS_CARPET, Material.PALE_MOSS_CARPET,
            Material.TORCH, Material.SOUL_TORCH, Material.REDSTONE_TORCH, Material.WALL_TORCH,
            Material.SOUL_WALL_TORCH, Material.REDSTONE_WALL_TORCH, Material.LANTERN, Material.SOUL_LANTERN,
            Material.TRIPWIRE, Material.TRIPWIRE_HOOK, Material.STRING, Material.LEVER,
            Material.STONE_BUTTON, Material.OAK_BUTTON, Material.SPRUCE_BUTTON, Material.BIRCH_BUTTON,
            Material.JUNGLE_BUTTON, Material.ACACIA_BUTTON, Material.DARK_OAK_BUTTON, Material.CRIMSON_BUTTON,
            Material.WARPED_BUTTON, Material.POLISHED_BLACKSTONE_BUTTON, Material.RAIL, Material.POWERED_RAIL,
            Material.DETECTOR_RAIL, Material.ACTIVATOR_RAIL, Material.COBWEB, Material.LADDER,
            Material.SCAFFOLDING, Material.SNOW, Material.POWDER_SNOW, Material.WATER,
            Material.LAVA, Material.FIRE, Material.SOUL_FIRE
    );

    /**
     * Check if a block can be passed through by the chain
     */
    private boolean isPassableBlock(org.bukkit.block.Block block) {
        if (block == null) return true;
        Material type = block.getType();
        return type.isAir() || PASSABLE_BLOCKS.contains(type);
    }
}