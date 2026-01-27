package net.saturn.elementSmp.elements.abilities.impl.life.passives;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.config.Constants;
import net.saturn.elementSmp.elements.ElementType;
import net.saturn.elementSmp.managers.ElementManager;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Handles Life element passive 2: Faster crop growth in radius
 */
public class LifeCropPassive implements Listener {
    private final ElementSmp plugin;
    private final ElementManager elementManager;

    public LifeCropPassive(ElementSmp plugin, ElementManager elementManager) {
        this.plugin = plugin;
        this.elementManager = elementManager;
        startCropGrowthTask();
    }

    private void startCropGrowthTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (elementManager.getPlayerElement(player) != ElementType.LIFE) {
                        continue;
                    }

                    var playerData = elementManager.data(player.getUniqueId());
                    if (playerData.getUpgradeLevel(ElementType.LIFE) < 2) {
                        continue;
                    }

                    int radius = (int) Constants.Distance.LIFE_CROP_RADIUS;
                    for (int dx = -radius; dx <= radius; dx++) {
                        for (int dz = -radius; dz <= radius; dz++) {
                            for (int dy = -1; dy <= 1; dy++) {
                                Block block = player.getLocation().clone().add(dx, dy, dz).getBlock();
                                growIfCrop(block);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, Constants.Timing.TWO_SECONDS);
    }

    private void growIfCrop(Block block) {
        if (block.getBlockData() instanceof Ageable ageable) {
            if (ageable.getAge() < ageable.getMaximumAge()) {
                ageable.setAge(ageable.getMaximumAge());
                block.setBlockData(ageable);
            }
        }
    }
}
