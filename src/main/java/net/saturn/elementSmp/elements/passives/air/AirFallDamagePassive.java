package net.saturn.elementsmp.elements.passives.air;

import net.saturn.elementsmp.elements.core.ElementType;
import net.saturn.elementsmp.managers.ElementManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class AirFallDamagePassive implements Listener {
    private final ElementManager elementManager;

    public AirFallDamagePassive(ElementManager elementManager) {
        this.elementManager = elementManager;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;

        // Check if damage is from falling
        if (e.getCause() != EntityDamageEvent.DamageCause.FALL) return;

        // Check if player has Air element and Upgrade II
        var pd = elementManager.data(player.getUniqueId());
        if (pd == null || pd.getCurrentElement() != ElementType.AIR || pd.getUpgradeLevel(ElementType.AIR) < 2) return;

        // If they fall on pointed dripstone, they still take damage
        if (player.getLocation().getBlock().getType() == Material.POINTED_DRIPSTONE ||
            player.getLocation().clone().subtract(0, 0.1, 0).getBlock().getType() == Material.POINTED_DRIPSTONE) {
            return;
        }

        // Cancel fall damage for Air element players with Upgrade II
        e.setCancelled(true);
    }
}
