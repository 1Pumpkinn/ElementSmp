package net.saturn.elementSmp.listeners;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.gui.ElementSelectionGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

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

        String title = event.getView().getTitle();
        if (title.contains("Rolling Element") || title.contains("Select Your Element")) {
            event.setCancelled(true);

            ElementSelectionGUI gui = ElementSelectionGUI.getGUI(player.getUniqueId());
            if (gui != null) {
                gui.handleClick(event.getRawSlot());
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        String title = event.getView().getTitle();
        if (title.contains("Rolling Element") || title.contains("Select Your Element")) {
            ElementSelectionGUI.removeGUI(player.getUniqueId());
            InventoryCloseEvent.Reason reason = event.getReason();
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
    }


}
