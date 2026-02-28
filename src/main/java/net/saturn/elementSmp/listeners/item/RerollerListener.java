package net.saturn.elementSmp.listeners.item;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.gui.ElementSelectionGUI;
import net.saturn.elementSmp.data.PlayerData;
import net.saturn.elementSmp.elements.ElementType;
import net.saturn.elementSmp.items.ItemKeys;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class RerollerListener implements Listener {
    private final ElementSmp plugin;
    private static final java.util.Map<java.util.UUID, Long> lastWarnAt = new java.util.concurrent.ConcurrentHashMap<>();
    private static final long WARN_COOLDOWN_MS = 1000L;

    public RerollerListener(ElementSmp plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRerollerUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || !item.hasItemMeta()) return;

        var container = item.getItemMeta().getPersistentDataContainer();
        boolean isReroller = container.has(ItemKeys.reroller(plugin), PersistentDataType.BYTE);
        boolean isAdvancedReroller = container.has(ItemKeys.advancedReroller(plugin), PersistentDataType.BYTE);

        if (isReroller || isAdvancedReroller) {
            org.bukkit.event.block.Action action = event.getAction();
            if (action != org.bukkit.event.block.Action.RIGHT_CLICK_AIR &&
                    action != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
                return;
            }

            event.setCancelled(true);

            var store = plugin.getDataStore();
            if (isReroller && !store.isRecipeEnabled("reroller")) {
                if (shouldWarn(player)) {
                    player.sendMessage(net.kyori.adventure.text.Component.text("Reroller is disabled by the server.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
                }
                return;
            }
            if (isAdvancedReroller && !store.isRecipeEnabled("advanced_reroller")) {
                if (shouldWarn(player)) {
                    player.sendMessage(net.kyori.adventure.text.Component.text("Advanced Reroller is disabled by the server.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
                }
                return;
            }

            // Check if holding any reroller in BOTH hands
            ItemStack mainHand = player.getInventory().getItemInMainHand();
            ItemStack offHand = player.getInventory().getItemInOffHand();

            if (isAnyReroller(mainHand) && isAnyReroller(offHand)) {
                if (shouldWarn(player)) {
                    player.sendMessage(net.kyori.adventure.text.Component.text("You cannot use rerollers while holding one in each hand!").color(net.kyori.adventure.text.format.NamedTextColor.RED));
                }
                return;
            }

            if (plugin.getElementManager().isCurrentlyRolling(player)) {
                if (shouldWarn(player)) {
                    player.sendMessage(net.kyori.adventure.text.Component.text("You are already rerolling your element!").color(net.kyori.adventure.text.format.NamedTextColor.RED));
                }
                return;
            }

            // Only proceed if this is a normal Reroller (Advanced is handled in its own listener)
            if (!isReroller) return;

            PlayerData pd = plugin.getElementManager().data(player.getUniqueId());
            ElementType oldElement = pd.getCurrentElement();
            if (oldElement != null) {
                clearOldElementEffects(player, oldElement);
            }

            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                player.getInventory().removeItem(item);
            }

            new ElementSelectionGUI(plugin, player, true).open();
        }
    }

    private boolean isAnyReroller(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        var container = item.getItemMeta().getPersistentDataContainer();
        return container.has(ItemKeys.reroller(plugin), PersistentDataType.BYTE) ||
               container.has(ItemKeys.advancedReroller(plugin), PersistentDataType.BYTE);
    }

    private boolean shouldWarn(Player player) {
        long now = System.currentTimeMillis();
        java.util.UUID id = player.getUniqueId();
        Long last = lastWarnAt.get(id);
        if (last == null || now - last > WARN_COOLDOWN_MS) {
            lastWarnAt.put(id, now);
            return true;
        }
        return false;
    }

    private void clearOldElementEffects(Player player, ElementType oldElement) {
        if (oldElement == null) return;

        var element = plugin.getElementManager().get(oldElement);
        if (element != null) {
            element.clearEffects(player);
        }

        if (oldElement == ElementType.LIFE) {
            var attr = player.getAttribute(Attribute.MAX_HEALTH);
            if (attr != null) {
                attr.setBaseValue(20.0);
                if (!player.isDead() && player.getHealth() > 0 && player.getHealth() > 20.0) {
                    player.setHealth(20.0);
                }
            }
        }
    }
}


