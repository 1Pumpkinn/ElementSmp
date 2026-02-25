package net.saturn.elementSmp.listeners.item;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.data.PlayerData;
import net.saturn.elementSmp.elements.ElementType;
import net.saturn.elementSmp.items.ItemKeys;
import net.saturn.elementSmp.managers.ElementManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class ElementItemCraftListener implements Listener {
    private final ElementSmp plugin;
    private final ElementManager elements;

    public ElementItemCraftListener(ElementSmp plugin, ElementManager elements) {
        this.plugin = plugin;
        this.elements = elements;
    }

    @EventHandler
    public void onCraft(CraftItemEvent e) {
        if (!(e.getWhoClicked() instanceof org.bukkit.entity.Player p)) return;
        ItemStack result = e.getRecipe() == null ? null : e.getRecipe().getResult();
        if (result == null) return;
        ItemMeta meta = result.getItemMeta();
        if (meta == null) return;

        Integer level = meta.getPersistentDataContainer().get(ItemKeys.upgraderLevel(plugin), PersistentDataType.INTEGER);
        if (level != null) {
            return;
        }
    }

    private void handleUpgraderCrafting(CraftItemEvent e, org.bukkit.entity.Player p, Integer level) {
        return;
    }

}


