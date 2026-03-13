package net.saturn.elementsmp.recipes;

import net.saturn.elementsmp.ElementSmp;
import net.saturn.elementsmp.items.util.Upgrader1Item;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

public class Upgrader1Recipe {
    public static void register(ElementSmp plugin) {
        try {
            ItemStack result = Upgrader1Item.make(plugin);
            NamespacedKey key = new NamespacedKey(plugin, Upgrader1Item.KEY);
            
            // Remove existing recipe if it exists
            plugin.getServer().removeRecipe(key);
            
            ShapedRecipe recipe = new ShapedRecipe(key, result);
            recipe.shape("GFG", "WDB", "GAG");
            recipe.setIngredient('G', Material.GOLD_BLOCK);
            recipe.setIngredient('D', Material.DIAMOND_BLOCK);

            recipe.setIngredient('F', Material.FIRE_CHARGE);
            recipe.setIngredient('W', Material.WATER_BUCKET);
            recipe.setIngredient('B', Material.GRASS_BLOCK);
            recipe.setIngredient('A', Material.FEATHER);

            boolean success = plugin.getServer().addRecipe(recipe);
            if (!success) {
                plugin.getLogger().warning("Failed to register Upgrader I recipe");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error registering Upgrader I recipe: " + e.getMessage());
        }
    }
}
