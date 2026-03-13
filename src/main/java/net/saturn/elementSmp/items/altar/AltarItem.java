package net.saturn.elementsmp.items.altar;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.saturn.elementsmp.ElementSmp;
import net.saturn.elementsmp.items.ItemKeys;
import net.saturn.elementsmp.items.builder.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

/**
 * Generic class for creating items produced by the Altar.
 * Decouples specific element items from their own classes.
 */
public final class AltarItem {
    
    private final ElementSmp plugin;
    private final String key;
    private final Material material;
    private final String displayName;
    private final NamedTextColor nameColor;
    private final List<Component> lore;

    public AltarItem(ElementSmp plugin, String key, Material material, String displayName, NamedTextColor nameColor, List<Component> lore) {
        this.plugin = plugin;
        this.key = key;
        this.material = material;
        this.displayName = displayName;
        this.nameColor = nameColor;
        this.lore = lore;
    }

    /**
     * Builds the ItemStack for this Altar item.
     * @return The created ItemStack
     */
    public ItemStack build() {
        return ItemBuilder.start(material)
                .name(Component.text(displayName)
                        .color(nameColor)
                        .decoration(TextDecoration.ITALIC, false)
                        .decoration(TextDecoration.BOLD, true))
                .lore(lore)
                .data(ItemKeys.namespaced(plugin, key), PersistentDataType.BYTE, (byte) 1)
                .build();
    }

    /**
     * Helper to create a standard "Soul" style item for an element.
     */
    public static ItemStack createSoul(ElementSmp plugin, String elementKey, Material material, NamedTextColor color, String description) {
        return new AltarItem(
                plugin,
                elementKey + "_element",
                material,
                capitalize(elementKey) + " Element",
                color,
                List.of(
                        Component.text(description).color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                        Component.text("Right-click to become the " + capitalize(elementKey) + " Element.").color(color).decoration(TextDecoration.ITALIC, false)
                )
        ).build();
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
