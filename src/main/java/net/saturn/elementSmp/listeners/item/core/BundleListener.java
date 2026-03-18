package net.saturn.elementsmp.listeners.item.core;

import net.saturn.elementsmp.ElementSmp;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BundleListener implements Listener {
    private final ElementSmp plugin;
    private static final String BUNDLE_TITLE = ChatColor.DARK_PURPLE + "Bundle";
    private final Map<UUID, ItemStack> openBundles = new HashMap<>();

    public BundleListener(ElementSmp plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onBundleRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        ItemStack item = event.getItem();
        if (item == null || !isBundle(item)) return;

        // Cancel vanilla behavior
        event.setCancelled(true);
        Player player = event.getPlayer();
        
        openBundleInventory(player, item);
        player.playSound(player.getLocation(), Sound.ITEM_BUNDLE_DROP_CONTENTS, 1.0f, 1.0f);
    }

    private boolean isBundle(ItemStack item) {
        if (item == null) return false;
        return item.getType().name().endsWith("BUNDLE");
    }

    private void openBundleInventory(Player player, ItemStack bundle) {
        Inventory inv = Bukkit.createInventory(player, 9, BUNDLE_TITLE);
        
        if (bundle.getItemMeta() instanceof BundleMeta meta) {
            List<ItemStack> items = meta.getItems();
            for (int i = 0; i < Math.min(items.size(), 9); i++) {
                inv.setItem(i, items.get(i));
            }
        }
        
        openBundles.put(player.getUniqueId(), bundle);
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(BUNDLE_TITLE)) {
            ItemStack currentItem = event.getCurrentItem();
            ItemStack cursorItem = event.getCursor();
            
            // Prevent putting bundles inside bundles via cursor or clicking
            if (isBundle(currentItem) || isBundle(cursorItem)) {
                event.setCancelled(true);
                if (event.getRawSlot() < event.getInventory().getSize()) {
                    event.getWhoClicked().sendMessage(ChatColor.RED + "You cannot put a bundle inside another bundle!");
                } else {
                    event.getWhoClicked().sendMessage(ChatColor.RED + "You cannot move bundles while using bundle storage!");
                }
                return;
            }

            // Block hotbar swapping bundles into storage
            if (event.getClick() == ClickType.NUMBER_KEY) {
                ItemStack hotbarItem = event.getWhoClicked().getInventory().getItem(event.getHotbarButton());
                if (isBundle(hotbarItem)) {
                    event.setCancelled(true);
                    event.getWhoClicked().sendMessage(ChatColor.RED + "You cannot swap bundles into storage!");
                    return;
                }
            }

            // Block shift-clicking bundles
            if (event.getClick().isShiftClick() && isBundle(currentItem)) {
                event.setCancelled(true);
                event.getWhoClicked().sendMessage(ChatColor.RED + "You cannot shift-click bundles into storage!");
                return;
            }
            return;
        }

        // Open bundle from inventory
        if (event.getClick() == ClickType.RIGHT && isBundle(event.getCurrentItem())) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            openBundleInventory(player, event.getCurrentItem());
            player.playSound(player.getLocation(), Sound.ITEM_BUNDLE_DROP_CONTENTS, 1.0f, 1.0f);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!event.getView().getTitle().equals(BUNDLE_TITLE)) return;

        if (isBundle(event.getOldCursor())) {
            event.setCancelled(true);
            event.getWhoClicked().sendMessage(ChatColor.RED + "You cannot drag bundles into storage!");
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getView().getTitle().equals(BUNDLE_TITLE)) return;

        Player player = (Player) event.getPlayer();
        ItemStack bundle = openBundles.remove(player.getUniqueId());

        if (isBundle(bundle)) {
            saveBundleInventory(bundle, event.getInventory());
            player.playSound(player.getLocation(), Sound.ITEM_BUNDLE_INSERT, 1.0f, 1.0f);
        }
    }

    private void saveBundleInventory(ItemStack bundle, Inventory inv) {
        bundle.editMeta(BundleMeta.class, meta -> {
            List<ItemStack> items = new ArrayList<>();
            for (int i = 0; i < 9; i++) {
                ItemStack item = inv.getItem(i);
                if (item != null && item.getType() != Material.AIR) {
                    items.add(item);
                }
            }
            meta.setItems(items);
        });
    }
}
