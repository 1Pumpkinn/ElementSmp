package net.saturn.elementSmp.listeners.item;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.data.PlayerData;
import net.saturn.elementSmp.elements.ElementType;
import net.saturn.elementSmp.items.ItemKeys;
import net.saturn.elementSmp.items.LightningSoulItem;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class LightningSoulListener implements Listener {
    private final ElementSmp plugin;

    public LightningSoulListener(ElementSmp plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || !item.hasItemMeta()) return;
        var meta = item.getItemMeta();
        var container = meta.getPersistentDataContainer();

        if (!container.has(ItemKeys.namespaced(plugin, LightningSoulItem.KEY), PersistentDataType.BYTE)) return;

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        event.setCancelled(true);

        if (plugin.getElementManager().getPlayerElement(player) == ElementType.LIGHTNING) {
            player.sendMessage(ChatColor.RED + "You already possess the Lightning Element!");
            return;
        }

        // Consume item
        item.setAmount(item.getAmount() - 1);
        if (item.getAmount() <= 0) player.getInventory().removeItem(item);

        // Change element via ElementManager
        plugin.getElementManager().setElement(player, ElementType.LIGHTNING);

        // Effects
        player.getWorld().strikeLightningEffect(player.getLocation());
        player.getWorld().spawnParticle(Particle.GLOW, player.getLocation(), 100, 1, 2, 1, 0.2);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
        
        player.sendMessage(ChatColor.YELLOW + "The power of the storm now flows through you. You are the Lightning Element!");
    }
}
