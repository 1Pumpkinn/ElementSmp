package net.saturn.elementsmp.recipes;

import net.saturn.elementsmp.ElementSmp;
import net.saturn.elementsmp.items.util.AdvancedRerollerItem;
import net.saturn.elementsmp.items.util.RerollerItem;
import net.saturn.elementsmp.items.util.Upgrader1Item;
import net.saturn.elementsmp.items.util.Upgrader2Item;

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


