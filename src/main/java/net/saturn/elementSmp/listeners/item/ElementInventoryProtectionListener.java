package net.saturn.elementSmp.listeners.item;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.elements.ElementType;
import net.saturn.elementSmp.managers.ElementManager;
import net.saturn.elementSmp.util.bukkit.ItemUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ElementInventoryProtectionListener implements Listener {
    private final ElementSmp plugin;
    private final ElementManager elements;

    public ElementInventoryProtectionListener(ElementSmp plugin, ElementManager elements) {
        this.plugin = plugin;
        this.elements = elements;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        // No longer restricting Life/Death cores as they are removed from crafting
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        // No longer restricting Life/Death cores as they are removed from crafting
    }
}


