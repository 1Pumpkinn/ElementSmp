package net.saturn.elementSmp.altar;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public record AltarRecipe(String name, Map<Material, Integer> ingredients, ItemStack result) {
}
