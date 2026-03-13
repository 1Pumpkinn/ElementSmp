package net.saturn.elementsmp.recipes;

import net.saturn.elementsmp.ElementSmp;
import net.saturn.elementsmp.items.util.Upgrader2Item;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

public class Upgrader2Recipe {
    public static void register(ElementSmp plugin) {
        try {
            ItemStack result = Upgrader2Item.make(plugin);
            NamespacedKey key = new NamespacedKey(plugin, Upgrader2Item.KEY);
            
            // Remove existing recipe if it exists
            plugin.getServer().removeRecipe(key);
            
            ShapedRecipe recipe = new ShapedRecipe(key, result);
            recipe.shape("DFD", "WNB", "DAD");
            recipe.setIngredient('D', Material.DIAMOND_BLOCK);
            recipe.setIngredient('N', Material.NETHERITE_INGOT);

            recipe.setIngredient('F', Material.FIRE_CHARGE);
            recipe.setIngredient('W', Material.WATER_BUCKET);
            recipe.setIngredient('B', Material.GRASS_BLOCK);
            recipe.setIngredient('A', Material.FEATHER);
            
            boolean success = plugin.getServer().addRecipe(recipe);
            if (!success) {
                plugin.getLogger().warning("Failed to register Upgrader II recipe");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error registering Upgrader II recipe: " + e.getMessage());
        }
    }
}
