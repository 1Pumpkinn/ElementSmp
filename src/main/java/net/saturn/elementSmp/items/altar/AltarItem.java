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
                .data(ItemKeys.namespaced(plugin, ItemKeys.KEY_ALTAR_ITEM), PersistentDataType.BYTE, (byte) 1)
                .build();
    }

    /**
     * Helper to create a standard "Soul" style item for an element.
     */
    public static ItemStack createSoul(ElementSmp plugin, String elementKey, Material material, NamedTextColor color, String description) {
        return new AltarItem(
                plugin,
                elementKey,
                material,
                capitalize(elementKey) + " Altar Item",
                color,
                List.of(
                        Component.text(description).color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                        Component.text("Right-click to become the " + capitalize(elementKey) + " Element.").color(color).decoration(TextDecoration.ITALIC, false)
                )
        ).build();
    }

    /**
     * Gets the altar item for a specific element type.
     */
    public static ItemStack soulFor(net.saturn.elementsmp.elements.core.ElementType type, ElementSmp plugin) {
        return switch (type) {
            case AIR -> createSoul(plugin, "air", Material.FEATHER, NamedTextColor.WHITE, "The essence of the wind itself.");
            case WATER -> createSoul(plugin, "water", Material.WATER_BUCKET, NamedTextColor.BLUE, "A fluid, ever-changing core.");
            case FIRE -> createSoul(plugin, "fire", Material.FIRE_CHARGE, NamedTextColor.RED, "A burning, volatile energy.");
            case EARTH -> createSoul(plugin, "earth", Material.GRASS_BLOCK, NamedTextColor.GREEN, "Solid, unyielding stone.");
            case LIFE -> createSoul(plugin, "life", Material.APPLE, NamedTextColor.LIGHT_PURPLE, "A vibrant, growing essence.");
            case DEATH -> createSoul(plugin, "death", Material.WITHER_SKELETON_SKULL, NamedTextColor.DARK_GRAY, "A dark, decaying core.");
            case METAL -> createSoul(plugin, "metal", Material.IRON_INGOT, NamedTextColor.GRAY, "Hardened, tempered steel.");
            case FROST -> createSoul(plugin, "frost", Material.SNOWBALL, NamedTextColor.AQUA, "A frozen, chilling core.");
            case LIGHTNING -> createSoul(plugin, "lightning", Material.LIGHTNING_ROD, NamedTextColor.YELLOW, "A core pulsing with raw electrical energy.");
        };
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
