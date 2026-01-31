package net.saturn.elementSmp.listeners.item;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.data.PlayerData;
import net.saturn.elementSmp.elements.ElementType;
import net.saturn.elementSmp.gui.ElementSelectionGUI;
import net.saturn.elementSmp.items.ItemKeys;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Random;

public class AdvancedRerollerListener implements Listener {
    private final ElementSmp plugin;
    private final Random random = new Random();

    public AdvancedRerollerListener(ElementSmp plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAdvancedRerollerUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || !item.hasItemMeta()) return;
        var meta = item.getItemMeta();
        var container = meta.getPersistentDataContainer();

        boolean isAdvancedReroller = container.has(ItemKeys.advancedReroller(plugin), PersistentDataType.BYTE);
        boolean isReroller = container.has(ItemKeys.reroller(plugin), PersistentDataType.BYTE);

        if (!isAdvancedReroller && !isReroller) return;

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        event.setCancelled(true);

        // Check if holding any reroller in BOTH hands
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        if (isAnyReroller(mainHand) && isAnyReroller(offHand)) {
            player.sendMessage(ChatColor.RED + "You cannot use rerollers while holding one in each hand!");
            return;
        }

        if (plugin.getElementManager().isCurrentlyRolling(player)) {
            player.sendMessage(ChatColor.RED + "You are already rerolling your element!");
            return;
        }

        // Only proceed if this is an Advanced Reroller
        if (!isAdvancedReroller) return;

        item.setAmount(item.getAmount() - 1);
        if (item.getAmount() <= 0) player.getInventory().removeItem(item);

        ElementType[] pool = {ElementType.METAL, ElementType.FROST};
        new ElementSelectionGUI(plugin, player, true, pool).open();
    }

    private boolean isAnyReroller(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        var container = item.getItemMeta().getPersistentDataContainer();
        return container.has(ItemKeys.reroller(plugin), PersistentDataType.BYTE) ||
               container.has(ItemKeys.advancedReroller(plugin), PersistentDataType.BYTE);
    }

    private void clearOldElementEffects(Player player, PlayerData pd) {
        ElementType oldElement = pd.getCurrentElement();

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


