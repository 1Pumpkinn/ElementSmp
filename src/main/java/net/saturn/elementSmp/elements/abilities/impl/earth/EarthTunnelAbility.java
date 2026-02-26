package net.saturn.elementSmp.elements.abilities.impl.earth;

import net.saturn.elementSmp.elements.ElementContext;
import net.saturn.elementSmp.elements.abilities.BaseAbility;
import net.saturn.elementSmp.elements.impl.earth.EarthElement;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.EnumSet;
import java.util.Set;

public class EarthTunnelAbility extends BaseAbility {
    private static final Set<Material> TUNNELABLE = EnumSet.of(
            Material.STONE, Material.DEEPSLATE, Material.DIRT, Material.GRASS_BLOCK,
            Material.COBBLESTONE, Material.ANDESITE, Material.DIORITE, Material.GRANITE,

            Material.GRAVEL, Material.SAND, Material.RED_SAND, Material.CLAY, Material.MOSS_BLOCK, Material.SANDSTONE,
            Material.TUFF, Material.CALCITE, Material.DRIPSTONE_BLOCK,

            // Nether Blocks
            Material.BLACKSTONE, Material.ANCIENT_DEBRIS, Material.CRIMSON_NYLIUM,
            Material.WARPED_HYPHAE, Material.SOUL_SAND, Material.BASALT, Material.SOUL_SOIL,
            Material.NETHERRACK,

            // Ores

            Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE,
            Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE,
            Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE,
            Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE,
            Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE,
            Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE,
            Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE,
            Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE,

            Material.NETHERITE_BLOCK,
            Material.DIAMOND_BLOCK,
            Material.EMERALD_BLOCK,
            Material.GOLD_BLOCK,
            Material.REDSTONE_BLOCK,
            Material.LAPIS_BLOCK,
            Material.IRON_BLOCK,
            Material.COPPER_BLOCK,
            Material.COAL_BLOCK
    );

    private final net.saturn.elementSmp.ElementSmp plugin;

    public EarthTunnelAbility(net.saturn.elementSmp.ElementSmp plugin) {
        super();
        this.plugin = plugin;
    }

    @Override
    public boolean execute(ElementContext context) {
        Player player = context.getPlayer();

        // If ability is already active (check metadata), cancel it WITHOUT consuming mana
        if (player.hasMetadata(EarthElement.META_TUNNELING)) {
            player.removeMetadata(EarthElement.META_TUNNELING, plugin);
            player.sendMessage(ChatColor.YELLOW + "Tunneling cancelled");
            setActive(player, false);
            // Don't start a new tunnel - just cancel and return
            return true;
        }

        // Start the tunneling ability
        player.setMetadata(EarthElement.META_TUNNELING, new FixedMetadataValue(plugin, System.currentTimeMillis() + 30_000L));
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_STONE_BREAK, 1f, 0);
        player.sendMessage(ChatColor.GOLD + "Tunneling started Press again to cancel.");

        setActive(player, true);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !player.hasMetadata(EarthElement.META_TUNNELING)) {
                    setActive(player, false);
                    cancel();
                    return;
                }

                long until = player.getMetadata(EarthElement.META_TUNNELING).get(0).asLong();
                if (System.currentTimeMillis() > until) {
                    player.removeMetadata(EarthElement.META_TUNNELING, plugin);
                    player.sendMessage(ChatColor.YELLOW + "Tunneling ended");
                    setActive(player, false);
                    cancel();
                    return;
                }

                Location eyeLocation = player.getEyeLocation();
                Block targetBlock = player.getTargetBlockExact(4);
                Location mineLocation;
                if (targetBlock != null) {
                    mineLocation = targetBlock.getLocation().add(0.5, 0.5, 0.5);
                } else {
                    Vector direction = eyeLocation.getDirection().normalize();
                    mineLocation = eyeLocation.clone().add(direction.multiply(2.0));
                }
                breakTunnel(mineLocation, player);

                player.getWorld().spawnParticle(Particle.BLOCK, mineLocation, 10, 0.5, 0.5, 0.5, 0.1, Material.ANDESITE.createBlockData(), true);
            }
        }.runTaskTimer(plugin, 0L, 5L);

        return true;
    }

    private void breakTunnel(Location center, Player player) {
        World world = center.getWorld();
        if (world == null) return;

        boolean brokeAny = false;

        int baseX = center.getBlockX();
        int baseY = center.getBlockY();
        int baseZ = center.getBlockZ();

        for (int x = -1; x <= 1; x++) {
            int bx = baseX + x;
            for (int y = -1; y <= 1; y++) {
                int by = baseY + y;
                for (int z = -1; z <= 1; z++) {
                    int bz = baseZ + z;
                    Block block = world.getBlockAt(bx, by, bz);

                    if (TUNNELABLE.contains(block.getType())) {
                        block.breakNaturally();
                        brokeAny = true;
                    }
                }
            }
        }

        if (brokeAny) {
            world.playSound(center, Sound.BLOCK_TUFF_BREAK, 0.5f, 0.6f);
        }
    }
}
