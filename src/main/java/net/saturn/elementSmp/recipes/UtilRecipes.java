package net.saturn.elementSmp.recipes;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.items.AdvancedRerollerItem;
import net.saturn.elementSmp.items.RerollerItem;
import net.saturn.elementSmp.items.Upgrader1Item;
import net.saturn.elementSmp.items.Upgrader2Item;

public class UtilRecipes {
    public static void registerRecipes(ElementSmp plugin) {
        Upgrader1Item.registerRecipe(plugin);
        Upgrader2Item.registerRecipe(plugin);
        RerollerItem.registerRecipe(plugin);
        
        if (plugin.getConfigManager().isAdvancedRerollerRecipeEnabled()) {
            AdvancedRerollerItem.registerRecipe(plugin);
        }
    }
}


