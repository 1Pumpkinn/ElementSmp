package net.saturn.elementsmp.listeners.item.altar;

import net.saturn.elementsmp.ElementSmp;
import net.saturn.elementsmp.elements.core.ElementType;
import net.saturn.elementsmp.items.ItemKeys;
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

import java.util.Optional;

/**
 * Generic listener for all Altar items that change a player's element.
 */
public class AltarElementItemListener implements Listener {
    private final ElementSmp plugin;

    public AltarElementItemListener(ElementSmp plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || !item.hasItemMeta()) return;
        var meta = item.getItemMeta();
        var container = meta.getPersistentDataContainer();

        // Check all element types to see if this item matches one
        Optional<ElementType> matchedType = Optional.empty();
        for (ElementType type : ElementType.values()) {
            String key = type.name().toLowerCase() + "_element";
            if (container.has(ItemKeys.namespaced(plugin, key), PersistentDataType.BYTE)) {
                matchedType = Optional.of(type);
                break;
            }
        }

        if (matchedType.isEmpty()) return;
        ElementType type = matchedType.get();

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        event.setCancelled(true);

        if (plugin.getElementManager().getPlayerElement(player) == type) {
            player.sendMessage(ChatColor.RED + "You already possess the " + capitalize(type.name()) + " Element!");
            return;
        }

        // Consume item
        item.setAmount(item.getAmount() - 1);
        if (item.getAmount() <= 0) player.getInventory().removeItem(item);

        // Change element via ElementManager
        plugin.getElementManager().setElement(player, type);

        // Set altar element flag to true
        var pd = plugin.getElementManager().data(player.getUniqueId());
        pd.setAltarElement(true);
        plugin.getDataStore().save(pd);

        // Standard effects for element change
        playEffects(player, type);
        
        player.sendMessage(ChatColor.YELLOW + "The power of the " + type.name().toLowerCase() + " now flows through you.");
    }

    private void playEffects(Player player, ElementType type) {
        var loc = player.getLocation();
        var world = player.getWorld();

        if (type == ElementType.LIGHTNING) {
            world.strikeLightningEffect(loc);
            world.spawnParticle(Particle.GLOW, loc, 100, 1, 2, 1, 0.2);
            world.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
        } else {
            world.spawnParticle(Particle.HAPPY_VILLAGER, loc, 100, 1, 2, 1, 0.1);
            world.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
