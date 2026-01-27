package net.saturn.elementSmp.managers;

import net.saturn.elementSmp.ElementSmp;
import org.bukkit.inventory.ItemStack;

public class ItemManager {
    private final ElementSmp plugin;

    public ItemManager(ElementSmp plugin, ManaManager mana, ConfigManager configManager) {
        this.plugin = plugin;
    }

    /**
     * Creates an Upgrader1 item
     * @return The created ItemStack
     */
    public ItemStack createUpgrader1() {
        return net.saturn.elementSmp.items.Upgrader1Item.make(plugin);
    }
    
    /**
     * Creates an Upgrader2 item
     * @return The created ItemStack
     */
    public ItemStack createUpgrader2() {
        return net.saturn.elementSmp.items.Upgrader2Item.make(plugin);
    }
}
