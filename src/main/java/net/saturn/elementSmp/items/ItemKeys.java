package net.saturn.elementSmp.items;

import net.saturn.elementSmp.ElementSmp;
import org.bukkit.NamespacedKey;

public final class ItemKeys {
    private ItemKeys() {}

    public static final String KEY_UPGRADER_LEVEL = "upgrader_level";

    public static final String KEY_REROLLER = "element_reroller";
    public static final String KEY_ADVANCED_REROLLER = "advanced_reroller";
    public static final String KEY_GUI_ITEM = "rolling_gui_item";

    public static NamespacedKey namespaced(ElementSmp plugin, String key) {
        return new NamespacedKey(plugin, key);
    }

    public static NamespacedKey upgraderLevel(ElementSmp plugin) { return namespaced(plugin, KEY_UPGRADER_LEVEL); }
    public static NamespacedKey reroller(ElementSmp plugin) { return namespaced(plugin, KEY_REROLLER); }
    public static NamespacedKey advancedReroller(ElementSmp plugin) { return namespaced(plugin, KEY_ADVANCED_REROLLER); }
    public static NamespacedKey guiItem(ElementSmp plugin) { return namespaced(plugin, KEY_GUI_ITEM); }
}
