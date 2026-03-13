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

public final class AdvancedRerollerItem {
    private AdvancedRerollerItem() {}

    public static final String KEY = "advanced_reroller";

    public static ItemStack make(ElementSmp plugin) {
        return ItemBuilder.start(Material.RECOVERY_COMPASS)
                .name(Component.text("Advanced Reroller")
                        .color(NamedTextColor.DARK_PURPLE)
                        .decoration(TextDecoration.ITALIC, false))
                .customModelData(4)
                .lore(List.of(
                        Component.text("Unlocks advanced elements").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                        Component.text("Right-click to randomly reroll your element").color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false)
                ))
                .data(ItemKeys.namespaced(plugin, KEY), PersistentDataType.BYTE, (byte) 1)
                .build();
    }
}


