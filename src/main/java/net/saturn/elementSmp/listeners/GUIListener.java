package net.saturn.elementSmp.listeners;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.gui.ElementSelectionGUI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.inventory.ItemStack;

public class GUIListener implements Listener {
    private final ElementSmp plugin;
    // Prevent rapid re-open loops when inventories transition
    private final java.util.Set<java.util.UUID> suppressReopen = new java.util.HashSet<>();

    public GUIListener(ElementSmp plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // Global protection: prevent any movement of GUI-tagged items
        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();
        if (isGuiItem(current) || isGuiItem(cursor)) {
            event.setCancelled(true);
            event.setResult(org.bukkit.event.Event.Result.DENY);
            if (event.getCursor() != null && !event.getCursor().getType().isAir()) {
                event.setCursor(null);
            }
            Bukkit.getScheduler().runTask(plugin, player::updateInventory);
            return;
        }

        String title = event.getView().getTitle();
        if (title.contains("Rolling Element") || title.contains("Select Your Element")) {
            event.setCancelled(true);
            event.setResult(org.bukkit.event.Event.Result.DENY);
            if (event.getCursor() != null && !event.getCursor().getType().isAir()) {
                event.setCursor(null);
            }
            // Prevent hotbar swap and double-click item collection edge cases
            if (event.getHotbarButton() != -1) {
                event.setCancelled(true);
                event.setResult(org.bukkit.event.Event.Result.DENY);
            }
            // Force client inventory to resync
            Bukkit.getScheduler().runTask(plugin, player::updateInventory);
        }
    }

    @EventHandler
    public void onInventoryCreative(InventoryCreativeEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();
        if (isGuiItem(cursor) || isGuiItem(current)) {
            event.setCancelled(true);
            event.setResult(org.bukkit.event.Event.Result.DENY);
            if (event.getCursor() != null && !event.getCursor().getType().isAir()) {
                event.setCursor(null);
            }
            Bukkit.getScheduler().runTask(plugin, player::updateInventory);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        String title = event.getView().getTitle();
        if (title.contains("Rolling Element") || title.contains("Select Your Element")) {
            ElementSelectionGUI gui = ElementSelectionGUI.getGUI(player.getUniqueId());
            InventoryCloseEvent.Reason reason = event.getReason();

            // If it's a reroll and not finished, re-open it
            if (gui != null && !gui.isFinished()) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    if (reason == InventoryCloseEvent.Reason.OPEN_NEW ||
                            reason == InventoryCloseEvent.Reason.PLUGIN) {
                        return;
                    }
                    gui.open();
                });
                return;
            }

            // Original logic for initial element selection
            ElementSelectionGUI.removeGUI(player.getUniqueId());
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (suppressReopen.contains(player.getUniqueId())) return;
                if (reason == InventoryCloseEvent.Reason.OPEN_NEW ||
                        reason == InventoryCloseEvent.Reason.PLUGIN) {
                    return;
                }
                net.saturn.elementSmp.managers.ElementManager em = plugin.getElementManager();
                if (em.data(player.getUniqueId()).getCurrentElement() == null) {
                    player.sendMessage(net.kyori.adventure.text.Component.text("You must choose an element to play!").color(net.kyori.adventure.text.format.NamedTextColor.RED));
                    suppressReopen.add(player.getUniqueId());
                    new net.saturn.elementSmp.gui.ElementSelectionGUI(plugin, player, false).open();
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> suppressReopen.remove(player.getUniqueId()), 2L);
                }
            });
        }
        // Global cleanup: purge any GUI-tagged items that somehow landed in player inventory
        purgeGuiItems((Player) event.getPlayer());
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        // Block any drag involving GUI-tagged items
        if (isGuiItem(event.getOldCursor())) {
            event.setCancelled(true);
            event.setResult(org.bukkit.event.Event.Result.DENY);
            Bukkit.getScheduler().runTask(plugin, () -> ((Player) event.getWhoClicked()).updateInventory());
            return;
        }
        for (ItemStack stack : event.getNewItems().values()) {
            if (isGuiItem(stack)) {
                event.setCancelled(true);
                event.setResult(org.bukkit.event.Event.Result.DENY);
                Bukkit.getScheduler().runTask(plugin, () -> ((Player) event.getWhoClicked()).updateInventory());
                return;
            }
        }

        String title = event.getView().getTitle();
        if (title.contains("Rolling Element") || title.contains("Select Your Element")) {
            event.setCancelled(true);
            event.setResult(org.bukkit.event.Event.Result.DENY);
            Bukkit.getScheduler().runTask(plugin, () -> ((Player) event.getWhoClicked()).updateInventory());
        }
    }

    private boolean isGuiItem(ItemStack stack) {
        if (stack == null) return false;
        if (!stack.hasItemMeta()) return false;
        var meta = stack.getItemMeta();
        var pdc = meta.getPersistentDataContainer();
        return pdc.has(net.saturn.elementSmp.items.ItemKeys.guiItem(plugin), org.bukkit.persistence.PersistentDataType.BYTE);
    }

    private void purgeGuiItems(Player player) {
        var inv = player.getInventory();
        boolean changed = false;
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (isGuiItem(item)) {
                inv.setItem(i, null);
                changed = true;
            }
        }
        if (changed) {
            Bukkit.getScheduler().runTask(plugin, player::updateInventory);
        }
    }


}
