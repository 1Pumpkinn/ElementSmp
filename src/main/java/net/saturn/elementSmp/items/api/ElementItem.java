package net.saturn.elementsmp.items.api;

import net.saturn.elementsmp.ElementSmp;
import net.saturn.elementsmp.elements.core.ElementType;
import net.saturn.elementsmp.managers.ConfigManager;
import net.saturn.elementsmp.managers.ManaManager;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public interface ElementItem {
    ElementType getElementType();

    ItemStack create(ElementSmp plugin);

    void registerRecipe(ElementSmp plugin);

    boolean isItem(ItemStack stack, ElementSmp plugin);

    boolean handleUse(PlayerInteractEvent e, ElementSmp plugin, ManaManager mana, ConfigManager config);

    void handleDamage(EntityDamageByEntityEvent e, ElementSmp plugin);

    default void handleLaunch(ProjectileLaunchEvent e, ElementSmp plugin, ManaManager mana, ConfigManager config) {}
}

