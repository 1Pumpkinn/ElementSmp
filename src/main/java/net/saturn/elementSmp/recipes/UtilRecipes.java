package net.saturn.elementSmp.recipes;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.items.AdvancedRerollerItem;
import net.saturn.elementSmp.items.RerollerItem;
import net.saturn.elementSmp.items.Upgrader1Item;
import net.saturn.elementSmp.items.Upgrader2Item;

public class UtilRecipes {
    public static void registerRecipes(ElementSmp plugin) {
        var store = plugin.getDataStore();
        if (store.isRecipeEnabled("upgrader1")) {
            Upgrader1Item.registerRecipe(plugin);
        }
        if (store.isRecipeEnabled("upgrader2")) {
            Upgrader2Item.registerRecipe(plugin);
        }
        if (store.isRecipeEnabled("reroller")) {
            RerollerItem.registerRecipe(plugin);
        }
        if (store.isRecipeEnabled("advanced_reroller")) {
            AdvancedRerollerItem.registerRecipe(plugin);
        }
    }
}


