package net.saturn.elementSmp.tasks;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.managers.BlockReversionManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Projectile;
import org.bukkit.scheduler.BukkitRunnable;

public class IceTrailTask extends BukkitRunnable {
    private final Projectile projectile;
    private final BlockReversionManager blockReversionManager;
    private final ElementSmp plugin;

    public IceTrailTask(ElementSmp plugin, Projectile projectile, BlockReversionManager blockReversionManager) {
        this.plugin = plugin;
        this.projectile = projectile;
        this.blockReversionManager = blockReversionManager;
    }

    @Override
    public void run() {
        if (projectile.isDead() || projectile.isOnGround()) {
            this.cancel();
            return;
        }

        Location location = projectile.getLocation();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    Block block = location.clone().add(x, y, z).getBlock();
                    if (block.getType().isSolid() && !block.isLiquid() && block.getType() != Material.PACKED_ICE) {
                        blockReversionManager.addBlock(block.getLocation(), block.getBlockData());
                        block.setType(Material.PACKED_ICE);
                        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                            blockReversionManager.revertBlock(block.getLocation());
                        }, 200L); // 10 seconds
                    }
                }
            }
        }
    }
}