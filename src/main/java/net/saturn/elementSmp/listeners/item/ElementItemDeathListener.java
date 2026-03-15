package net.saturn.elementsmp.listeners.item;

import net.saturn.elementsmp.ElementSmp;
import net.saturn.elementsmp.data.PlayerData;
import net.saturn.elementsmp.elements.core.ElementType;
import net.saturn.elementsmp.items.altar.AltarItem;
import net.saturn.elementsmp.managers.ElementManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

public record ElementItemDeathListener(ElementSmp plugin, ElementManager elements) implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent e) {
        PlayerData pd = elements.data(e.getEntity().getUniqueId());
        ElementType currentElement = pd.getCurrentElement();

        // ONLY trigger element reset and reroll if the player has an altar element
        if (currentElement != null && pd.isAltarElement()) {
            // Drop the element altar item
            e.getDrops().add(AltarItem.soulFor(currentElement, plugin));

            int currentLevel = pd.getUpgradeLevel(currentElement);

            if (currentLevel > 0) {
                for (int i = 0; i < currentLevel; i++) {
                    if (i == 0) {
                        e.getDrops().add(plugin.getItemManager().createUpgrader1());
                    } else {
                        e.getDrops().add(plugin.getItemManager().createUpgrader2());
                    }
                }
            }

            // Reset element and upgrade level
            pd.setCurrentElement(null);
            pd.setAltarElement(false);
            pd.setNeedsReroll(true);
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


