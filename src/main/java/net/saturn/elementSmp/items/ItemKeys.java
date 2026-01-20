package net.saturn.elementSmp.items;

import net.saturn.elementSmp.ElementSmp;
import org.bukkit.NamespacedKey;

public final class ItemKeys {
    private ItemKeys() {}

    public static final String KEY_UPGRADER_LEVEL = "upgrader_level";

    public static final String KEY_ELEMENT_ITEM = "element_item";
    public static final String KEY_ELEMENT_TYPE = "element_type";

    public static final String KEY_REROLLER = "element_reroller";
    public static final String KEY_ADVANCED_REROLLER = "advanced_reroller";

    public static final String KEY_LIFE_CORE = "life_core";
    public static final String KEY_DEATH_CORE = "death_core";

    public static NamespacedKey namespaced(ElementSmp plugin, String key) {
        return new NamespacedKey(plugin, key);
    }

    public static NamespacedKey upgraderLevel(ElementSmp plugin) { return namespaced(plugin, KEY_UPGRADER_LEVEL); }
    public static NamespacedKey elementItem(ElementSmp plugin) { return namespaced(plugin, KEY_ELEMENT_ITEM); }
    public static NamespacedKey elementType(ElementSmp plugin) { return namespaced(plugin, KEY_ELEMENT_TYPE); }
    public static NamespacedKey reroller(ElementSmp plugin) { return namespaced(plugin, KEY_REROLLER); }
    public static NamespacedKey advancedReroller(ElementSmp plugin) { return namespaced(plugin, KEY_ADVANCED_REROLLER); }
    public static NamespacedKey lifeCore(ElementSmp plugin) { return namespaced(plugin, KEY_LIFE_CORE); }
    public static NamespacedKey deathCore(ElementSmp plugin) { return namespaced(plugin, KEY_DEATH_CORE); }
}
