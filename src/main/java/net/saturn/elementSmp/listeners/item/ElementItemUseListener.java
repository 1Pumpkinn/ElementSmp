package net.saturn.elementSmp.listeners.item;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.managers.ElementManager;
import net.saturn.elementSmp.managers.ItemManager;
import net.saturn.elementSmp.util.bukkit.ItemUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class ElementItemUseListener implements Listener {
	private final ElementSmp plugin;
	private final ElementManager elements;
	private final ItemManager itemManager;

	public ElementItemUseListener(ElementSmp plugin, ElementManager elements, ItemManager itemManager) {
		this.plugin = plugin;
		this.elements = elements;
		this.itemManager = itemManager;
	}

	private boolean isElementItem(ItemStack stack) {
		return ItemUtil.isElementItem(plugin, stack);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onInteract(PlayerInteractEvent event) {
		ItemStack inHand = event.getItem();
		if (inHand != null && isElementItem(inHand)) {
			if (net.saturn.elementSmp.items.CoreConsumptionHandler.handleCoreConsume(event, plugin, elements)) return;
			itemManager.handleUse(event);
		}
	}
}


