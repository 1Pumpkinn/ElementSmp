package net.saturn.elementsmp.recipes;

import net.saturn.elementsmp.ElementSmp;

public class UtilRecipes {
    public static void registerRecipes(ElementSmp plugin) {
        var store = plugin.getDataStore();
        if (store.isRecipeEnabled("upgrader1")) {
            Upgrader1Recipe.register(plugin);
        }
        if (store.isRecipeEnabled("upgrader2")) {
            Upgrader2Recipe.register(plugin);
        }
        if (store.isRecipeEnabled("reroller")) {
            RerollerRecipe.register(plugin);
        }
        if (store.isRecipeEnabled("advanced_reroller")) {
            AdvancedRerollerRecipe.register(plugin);
        }
    }
}


