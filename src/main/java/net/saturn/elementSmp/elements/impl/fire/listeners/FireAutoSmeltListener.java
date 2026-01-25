package net.saturn.elementSmp.elements.impl.fire.listeners;

import net.saturn.elementSmp.elements.ElementType;
import net.saturn.elementSmp.managers.ElementManager;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class FireAutoSmeltListener implements Listener {
    private final ElementManager elementManager;
    private static final Map<Material, Material> SMELT_MAP = new HashMap<>();

    static {
        SMELT_MAP.put(Material.RAW_IRON, Material.IRON_INGOT);
        SMELT_MAP.put(Material.RAW_GOLD, Material.GOLD_INGOT);
        SMELT_MAP.put(Material.RAW_COPPER, Material.COPPER_INGOT);
        SMELT_MAP.put(Material.IRON_ORE, Material.IRON_INGOT);
        SMELT_MAP.put(Material.GOLD_ORE, Material.GOLD_INGOT);
        SMELT_MAP.put(Material.COPPER_ORE, Material.COPPER_INGOT);
        SMELT_MAP.put(Material.DEEPSLATE_IRON_ORE, Material.IRON_INGOT);
        SMELT_MAP.put(Material.DEEPSLATE_GOLD_ORE, Material.GOLD_INGOT);
        SMELT_MAP.put(Material.DEEPSLATE_COPPER_ORE, Material.COPPER_INGOT);
    }

    public FireAutoSmeltListener(ElementManager elementManager) {
        this.elementManager = elementManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockDropItem(BlockDropItemEvent event) {
        if (elementManager.getPlayerElement(event.getPlayer()) != ElementType.FIRE) {
            return;
        }

        boolean smelted = false;
        for (org.bukkit.entity.Item itemEntity : event.getItems()) {
            ItemStack item = itemEntity.getItemStack();
            Material smeltedMaterial = SMELT_MAP.get(item.getType());
            
            if (smeltedMaterial != null) {
                item.setType(smeltedMaterial);
                smelted = true;
            }
        }

        if (smelted) {
            event.getPlayer().getWorld().playSound(event.getBlock().getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 1.5f);
        }
    }
}
