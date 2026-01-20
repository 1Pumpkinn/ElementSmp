package net.saturn.elementSmp.elements.abilities.impl.metal;

import net.saturn.elementSmp.ElementSmp;
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
    public static final String META_CHAINED_STUN = "metal_chain_stunned";

    public MetalChainAbility(ElementSmp plugin) {
        super("metal_chain", 75, 15, 2);
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
                entity -> (entity instanceof LivingEntity && !(entity instanceof org.bukkit.entity.ArmorStand) && !entity.equals(player))
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
                player.sendMessage(ChatColor.RED + "Your chain is blocked by a wall!");
                return false;
            }
        }

        if (target == null) {
            player.sendMessage(ChatColor.RED + "You must look at an entity to chain it!");
            return false;
        }

        // Don't target trusted players
        if (target instanceof Player targetPlayer) {
            if (context.getTrustManager().isTrusted(player.getUniqueId(), targetPlayer.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "You cannot chain trusted players!");
                return false;
            }
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

                    // Set metadata for stun duration (3 seconds = 3000ms)
                    long stunDuration = 3000; // 3 seconds in milliseconds
                    long stunUntil = System.currentTimeMillis() + stunDuration;
                    finalTarget.setMetadata(META_CHAINED_STUN, new FixedMetadataValue(plugin, stunUntil));

                    // Schedule metadata removal and re-enable AI after stun expires
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (finalTarget.isValid()) {
                                finalTarget.removeMetadata(META_CHAINED_STUN, plugin);
                                if (finalTarget instanceof Mob mob) {
                                    mob.setAware(true);
                                }
                            }
                        }
                    }.runTaskLater(plugin, 60L); // 3 seconds = 60 ticks

                    // Visual/audio feedback for stun
                    player.getWorld().playSound(targetLoc, Sound.BLOCK_ANVIL_LAND, 1.0f, 2.0f);
                    player.getWorld().spawnParticle(Particle.BLOCK, targetLoc, 30,
                            0.3, 0.5, 0.3, 0.1,
                            org.bukkit.Material.IRON_BLOCK.createBlockData(), true);

                    cancel();
                    return;
                }

                // Draw particle chain
                Vector direction = targetLoc.toVector().subtract(currentPlayerLoc.toVector()).normalize();
                double particleSpacing = 0.3;
                int numParticles = (int) (distance / particleSpacing);

                for (int i = 0; i <= numParticles; i++) {
                    double t = i * particleSpacing;
                    Location particleLoc = currentPlayerLoc.clone().add(direction.clone().multiply(t));

                    // Main chain particles (iron blocks/anvil look)
                    player.getWorld().spawnParticle(Particle.BLOCK, particleLoc, 1,
                            0.05, 0.05, 0.05, 0.0,
                            org.bukkit.Material.IRON_BLOCK.createBlockData(), true);

                    // Add some sparkles for effect
                    if (i % 3 == 0) {
                        player.getWorld().spawnParticle(Particle.CRIT, particleLoc, 1,
                                0.02, 0.02, 0.02, 0.01, null, true);
                    }
                }

                // Apply gentle pull force to target
                Vector pullDirection = currentPlayerLoc.toVector().subtract(targetLoc.toVector()).normalize();

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

    @Override
    public String getName() {
        return ChatColor.GRAY + "Chain Reel";
    }

    @Override
    public String getDescription() {
        return "Look at an enemy and pull them towards you with a chain. (75 mana)";
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

    /**
     * Check if a block can be passed through by the chain
     */
    private boolean isPassableBlock(org.bukkit.block.Block block) {
        if (block == null) return true;

        org.bukkit.Material type = block.getType();

        // Air is always passable
        if (type == org.bukkit.Material.AIR ||
                type == org.bukkit.Material.VOID_AIR ||
                type == org.bukkit.Material.CAVE_AIR) {
            return true;
        }

        // List of passable blocks that chains can go through
        return type == org.bukkit.Material.SHORT_GRASS ||
                type == org.bukkit.Material.TALL_GRASS ||
                type == org.bukkit.Material.FERN ||
                type == org.bukkit.Material.LARGE_FERN ||
                type == org.bukkit.Material.DEAD_BUSH ||
                type == org.bukkit.Material.DANDELION ||
                type == org.bukkit.Material.POPPY ||
                type == org.bukkit.Material.BLUE_ORCHID ||
                type == org.bukkit.Material.ALLIUM ||
                type == org.bukkit.Material.AZURE_BLUET ||
                type == org.bukkit.Material.RED_TULIP ||
                type == org.bukkit.Material.ORANGE_TULIP ||
                type == org.bukkit.Material.WHITE_TULIP ||
                type == org.bukkit.Material.PINK_TULIP ||
                type == org.bukkit.Material.OXEYE_DAISY ||
                type == org.bukkit.Material.CORNFLOWER ||
                type == org.bukkit.Material.LILY_OF_THE_VALLEY ||
                type == org.bukkit.Material.SUNFLOWER ||
                type == org.bukkit.Material.LILAC ||
                type == org.bukkit.Material.ROSE_BUSH ||
                type == org.bukkit.Material.PEONY ||
                type == org.bukkit.Material.SWEET_BERRY_BUSH ||
                type == org.bukkit.Material.BAMBOO ||
                type == org.bukkit.Material.SUGAR_CANE ||
                type == org.bukkit.Material.KELP ||
                type == org.bukkit.Material.SEAGRASS ||
                type == org.bukkit.Material.TALL_SEAGRASS ||
                type == org.bukkit.Material.WHEAT ||
                type == org.bukkit.Material.CARROTS ||
                type == org.bukkit.Material.POTATOES ||
                type == org.bukkit.Material.BEETROOTS ||
                type == org.bukkit.Material.MELON_STEM ||
                type == org.bukkit.Material.PUMPKIN_STEM ||
                type == org.bukkit.Material.VINE ||
                type == org.bukkit.Material.WEEPING_VINES ||
                type == org.bukkit.Material.WEEPING_VINES_PLANT ||
                type == org.bukkit.Material.TWISTING_VINES ||
                type == org.bukkit.Material.TWISTING_VINES_PLANT ||
                type == org.bukkit.Material.CAVE_VINES ||
                type == org.bukkit.Material.CAVE_VINES_PLANT ||
                type == org.bukkit.Material.GLOW_BERRIES ||
                type == org.bukkit.Material.TORCH ||
                type == org.bukkit.Material.REDSTONE_TORCH ||
                type == org.bukkit.Material.SOUL_TORCH ||
                type == org.bukkit.Material.REDSTONE_WIRE ||
                type == org.bukkit.Material.TRIPWIRE ||
                type == org.bukkit.Material.TRIPWIRE_HOOK ||
                type == org.bukkit.Material.LEVER ||
                type == org.bukkit.Material.STONE_BUTTON ||
                type == org.bukkit.Material.OAK_BUTTON ||
                type == org.bukkit.Material.SPRUCE_BUTTON ||
                type == org.bukkit.Material.BIRCH_BUTTON ||
                type == org.bukkit.Material.JUNGLE_BUTTON ||
                type == org.bukkit.Material.ACACIA_BUTTON ||
                type == org.bukkit.Material.DARK_OAK_BUTTON ||
                type == org.bukkit.Material.CRIMSON_BUTTON ||
                type == org.bukkit.Material.WARPED_BUTTON ||
                type == org.bukkit.Material.POLISHED_BLACKSTONE_BUTTON ||
                type == org.bukkit.Material.RAIL ||
                type == org.bukkit.Material.POWERED_RAIL ||
                type == org.bukkit.Material.DETECTOR_RAIL ||
                type == org.bukkit.Material.ACTIVATOR_RAIL ||
                type == org.bukkit.Material.COBWEB ||
                type == org.bukkit.Material.LADDER ||
                type == org.bukkit.Material.SCAFFOLDING ||
                type == org.bukkit.Material.SNOW ||
                type == Material.POWDER_SNOW ||
                type == org.bukkit.Material.WATER ||
                type == org.bukkit.Material.LAVA ||
                type == org.bukkit.Material.FIRE ||
                type == org.bukkit.Material.SOUL_FIRE;
    }
}