package net.saturn.elementsmp.items.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.saturn.elementsmp.ElementSmp;
import net.saturn.elementsmp.items.ItemKeys;
import net.saturn.elementsmp.items.builder.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
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
}
