package net.saturn.elementSmp.items.builder;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Builder class for creating ItemStacks with a fluent API
 */
public class ItemBuilder {
    private Material material;
    private Component name;
    private List<Component> lore = new ArrayList<>();
    private boolean unbreakable = false;
    private boolean hideAttributes = false;
    private boolean hideEnchants = false;
    private Integer customModelData;
    private int amount = 1;
    private List<Consumer<ItemMeta>> metaModifiers = new ArrayList<>();

    public ItemBuilder(Material material) {
        this.material = material;
    }

    public static ItemBuilder start(Material material) {
        return new ItemBuilder(material);
    }

    public ItemBuilder name(Component name) {
        this.name = name;
        return this;
    }

    public ItemBuilder lore(Component line) {
        this.lore.add(line);
        return this;
    }

    public ItemBuilder lore(List<Component> lore) {
        this.lore.addAll(lore);
        return this;
    }

    public ItemBuilder customModelData(int data) {
        this.customModelData = data;
        return this;
    }

    /**
     * Set the item as unbreakable
     * 
     * @return The builder instance
     */
    public ItemBuilder unbreakable() {
        this.unbreakable = true;
        return this;
    }
    
    /**
     * Hide item attributes
     * 
     * @return The builder instance
     */
    public ItemBuilder hideAttributes() {
        this.hideAttributes = true;
        return this;
    }

    /**
     * Hide enchantments
     * 
     * @return The builder instance
     */
    public ItemBuilder hideEnchants() {
        this.hideEnchants = true;
        return this;
    }
    
    /**
     * Set the item amount
     * 
     * @param amount The amount
     * @return The builder instance
     */
    public ItemBuilder amount(int amount) {
        this.amount = amount;
        return this;
    }

    /**
     * Add an enchantment
     * 
     * @param enchantment The enchantment
     * @param level The level
     * @return The builder instance
     */
    public ItemBuilder enchant(Enchantment enchantment, int level) {
        metaModifiers.add(meta -> meta.addEnchant(enchantment, level, true));
        return this;
    }

    /**
     * Add a persistent data value
     * 
     * @param key The key
     * @param value The value
     * @return The builder instance
     */
    public ItemBuilder data(org.bukkit.NamespacedKey key, String value) {
        metaModifiers.add(meta -> {
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(key, PersistentDataType.STRING, value);
        });
        return this;
    }

    /**
     * Build the ItemStack
     * 
     * @return The built ItemStack
     */
    public ItemStack build() {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            if (name != null) {
                meta.displayName(name);
            }
            
            if (!lore.isEmpty()) {
                meta.lore(lore);
            }
            
            if (customModelData != null) {
                meta.setCustomModelData(customModelData);
            }
            
            meta.setUnbreakable(unbreakable);
            
            if (hideAttributes) {
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            }
            
            if (hideEnchants) {
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            
            // Apply all meta modifiers
            for (Consumer<ItemMeta> modifier : metaModifiers) {
                modifier.accept(meta);
            }
            
            item.setItemMeta(meta);
        }
        
        return item;
    }

    public <T, Z> ItemBuilder data(NamespacedKey key, PersistentDataType<T, Z> type, Z value) {
        metaModifiers.add(meta -> {
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(key, type, value);
        });
        return this;
    }
}
