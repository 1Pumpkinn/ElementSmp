package net.saturn.elementSmp.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.items.builder.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public final class RerollerItem {
    private RerollerItem() {}

    public static final String KEY = "element_reroller";

    public static ItemStack make(ElementSmp plugin) {
        return ItemBuilder.start(Material.HEART_OF_THE_SEA)
                .name(Component.text("Element Reroller")
                        .color(NamedTextColor.LIGHT_PURPLE)
                        .decoration(TextDecoration.ITALIC, false))
                .customModelData(3)
                .lore(List.of(
                        Component.text("Allows you to change your element").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                        Component.text("Right-click to randomly reroll your element").color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false)
                ))
                .data(ItemKeys.reroller(plugin), PersistentDataType.BYTE, (byte) 1)
                .build();
    }

    public static void registerRecipe(ElementSmp plugin) {
        try {
            ItemStack result = make(plugin);
            NamespacedKey key = new NamespacedKey(plugin, KEY);
            
            // Remove existing recipe if it exists
            plugin.getServer().removeRecipe(key);
            
            ShapedRecipe recipe = new ShapedRecipe(key, result);
            recipe.shape("IEG", "ETE", "DEM");
            recipe.setIngredient('I', Material.IRON_BLOCK);
            recipe.setIngredient('G', Material.GOLD_BLOCK);
            recipe.setIngredient('D', Material.DIAMOND_BLOCK);
            recipe.setIngredient('M', Material.EMERALD_BLOCK);
            recipe.setIngredient('E', Material.NETHERITE_SCRAP);
            recipe.setIngredient('T', Material.TOTEM_OF_UNDYING);
            
            boolean success = plugin.getServer().addRecipe(recipe);
            if (!success) {
                plugin.getLogger().warning("Failed to register Element Reroller recipe");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error registering Element Reroller recipe: " + e.getMessage());
        }
    }
}
