package net.saturn.elementsmp.recipes;

import net.saturn.elementsmp.ElementSmp;
import net.saturn.elementsmp.items.util.RerollerItem;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

public class RerollerRecipe {
    public static void register(ElementSmp plugin) {
        try {
            ItemStack result = RerollerItem.make(plugin);
            NamespacedKey key = new NamespacedKey(plugin, RerollerItem.KEY);
            
            // Remove existing recipe if it exists
            plugin.getServer().removeRecipe(key);
            
            ShapedRecipe recipe = new ShapedRecipe(key, result);
            recipe.shape("IEG", "ETE", "DEM");
            recipe.setIngredient('I', Material.IRON_BLOCK);
            recipe.setIngredient('G', Material.GOLD_BLOCK);
            recipe.setIngredient('D', Material.DIAMOND_BLOCK);
            recipe.setIngredient('M', Material.EMERALD_BLOCK);
            recipe.setIngredient('E', Material.AMETHYST_SHARD);
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
