package net.saturn.elementSmp.listeners.item;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.data.PlayerData;
import net.saturn.elementSmp.elements.ElementType;
import net.saturn.elementSmp.managers.ElementManager;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public record ElementItemDeathListener(ElementSmp plugin, ElementManager elements) implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent e) {
        PlayerData pd = elements.data(e.getEntity().getUniqueId());
        ElementType currentElement = pd.getCurrentElement();

        if (currentElement != null) {
            int currentLevel = pd.getUpgradeLevel(currentElement);

            if (currentLevel > 0) {
                for (int i = 0; i < currentLevel; i++) {
                    if (i == 0) {
                        e.getDrops().add(plugin.getItemManager().createUpgrader1());
                    } else {
                        e.getDrops().add(plugin.getItemManager().createUpgrader2());
                    }
                }

                pd.setUpgradeLevel(currentElement, 0);
                plugin.getDataStore().save(pd);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (e.getEntity().isOnline()) {
                            elements.applyUpsides(e.getEntity());
                        }
                    }
                }.runTaskLater(plugin, 1L);
            }
        }
    }
}


