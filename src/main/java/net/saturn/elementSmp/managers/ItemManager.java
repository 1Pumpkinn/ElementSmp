package net.saturn.elementsmp.managers;

import net.saturn.elementsmp.ElementSmp;
import net.saturn.elementsmp.items.util.Upgrader1Item;
import net.saturn.elementsmp.items.util.Upgrader2Item;
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
        return Upgrader1Item.make(plugin);
    }
    
    /**
     * Creates an Upgrader2 item
     * @return The created ItemStack
     */
    public ItemStack createUpgrader2() {
        return Upgrader2Item.make(plugin);
    }
}
