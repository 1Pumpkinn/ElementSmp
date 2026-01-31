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

            // Check if holding any reroller in BOTH hands
            ItemStack mainHand = player.getInventory().getItemInMainHand();
            ItemStack offHand = player.getInventory().getItemInOffHand();

            if (isAnyReroller(mainHand) && isAnyReroller(offHand)) {
                player.sendMessage(net.kyori.adventure.text.Component.text("You cannot use rerollers while holding one in each hand!").color(net.kyori.adventure.text.format.NamedTextColor.RED));
                return;
            }

            if (plugin.getElementManager().isCurrentlyRolling(player)) {
                player.sendMessage(net.kyori.adventure.text.Component.text("You are already rerolling your element!").color(net.kyori.adventure.text.format.NamedTextColor.RED));
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

            new ElementSelectionGUI(plugin, player, true, ItemKeys.KEY_REROLLER).open();
        }
    }

    private boolean isAnyReroller(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        var container = item.getItemMeta().getPersistentDataContainer();
        return container.has(ItemKeys.reroller(plugin), PersistentDataType.BYTE) ||
               container.has(ItemKeys.advancedReroller(plugin), PersistentDataType.BYTE);
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


