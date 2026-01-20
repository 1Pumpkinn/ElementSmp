package net.saturn.elementSmp.util.bukkit;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.elements.ElementType;
import net.saturn.elementSmp.items.ItemKeys;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public final class ItemUtil {
    private ItemUtil() {}

    public static boolean isElementItem(ElementSmp plugin, ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) return false;
        Byte flag = stack.getItemMeta().getPersistentDataContainer()
                .get(ItemKeys.elementItem(plugin), PersistentDataType.BYTE);
        return flag != null && flag == (byte)1;
    }

    public static ElementType getElementType(ElementSmp plugin, ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) return null;
        String typeStr = stack.getItemMeta().getPersistentDataContainer()
                .get(ItemKeys.elementType(plugin), PersistentDataType.STRING);
        if (typeStr == null) return null;
        try {
            return ElementType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}


