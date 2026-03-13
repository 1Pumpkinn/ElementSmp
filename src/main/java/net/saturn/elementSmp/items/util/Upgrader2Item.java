package net.saturn.elementsmp.items.util;

import net.saturn.elementsmp.ElementSmp;
import net.saturn.elementsmp.items.ItemKeys;
import net.saturn.elementsmp.items.builder.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public final class Upgrader2Item {
    private Upgrader2Item() {}

    public static final String KEY = "upgrader_2";

    public static ItemStack make(ElementSmp plugin) {
        return ItemBuilder.start(Material.ECHO_SHARD)
                .name(Component.text("Upgrader II")
                        .color(NamedTextColor.AQUA)
                        .decoration(TextDecoration.ITALIC, false))
                .customModelData(2)
                .lore(List.of(
                        Component.text("Use by crafting to unlock").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                        Component.text("Ability 2 + Upside 2 for your element").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
                ))
                .data(ItemKeys.upgraderLevel(plugin), PersistentDataType.INTEGER, 2)
                .build();
    }
}
