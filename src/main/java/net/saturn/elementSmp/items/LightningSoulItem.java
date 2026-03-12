package net.saturn.elementSmp.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.items.builder.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public final class LightningSoulItem {
    private LightningSoulItem() {}

    public static final String KEY = "lightning_element";

    public static ItemStack make(ElementSmp plugin) {
        return ItemBuilder.start(Material.LIGHTNING_ROD)
                .name(Component.text("Lightning Element")
                        .color(NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false)
                        .decoration(TextDecoration.BOLD, true))
                .lore(List.of(
                        Component.text("A core pulsing with raw electrical energy.").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                        Component.text("Right-click to become the Lightning Element.").color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false)
                ))
                .data(ItemKeys.namespaced(plugin, KEY), PersistentDataType.BYTE, (byte) 1)
                .build();
    }
}
