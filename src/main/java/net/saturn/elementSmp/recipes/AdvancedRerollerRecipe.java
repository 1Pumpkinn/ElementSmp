package net.saturn.elementsmp.recipes;

import net.saturn.elementsmp.ElementSmp;
import net.saturn.elementsmp.items.util.AdvancedRerollerItem;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

public class AdvancedRerollerRecipe {
    public static void register(ElementSmp plugin) {
        try {
            ItemStack result = AdvancedRerollerItem.make(plugin);
            NamespacedKey key = new NamespacedKey(plugin, AdvancedRerollerItem.KEY);

            // Remove existing recipe if it exists
            plugin.getServer().removeRecipe(key);

            ShapedRecipe recipe = new ShapedRecipe(key, result);
            recipe.shape("DED", "ETE", "DED");
            recipe.setIngredient('D', Material.DIAMOND_BLOCK);
            recipe.setIngredient('E', Material.NETHERITE_INGOT);
            recipe.setIngredient('T', Material.TOTEM_OF_UNDYING);

            boolean success = plugin.getServer().addRecipe(recipe);
            if (!success) {
                plugin.getLogger().warning("Failed to register Advanced Reroller recipe");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error registering Advanced Reroller recipe: " + e.getMessage());
        }
    }
}
