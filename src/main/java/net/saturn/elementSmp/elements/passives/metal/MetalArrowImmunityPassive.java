package net.saturn.elementsmp.elements.passives.metal;

import net.saturn.elementsmp.elements.core.ElementType;
import net.saturn.elementsmp.managers.ElementManager;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class MetalArrowImmunityPassive implements Listener {
    private final ElementManager elementManager;

    public MetalArrowImmunityPassive(ElementManager elementManager) {
        this.elementManager = elementManager;
    }

    @EventHandler
    public void onArrowDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!(event.getDamager() instanceof Arrow)) return;

        // Check if player has Metal element with Upgrade 2
        var pd = elementManager.data(player.getUniqueId());
        if (pd.getCurrentElement() == ElementType.METAL && pd.getUpgradeLevel(ElementType.METAL) >= 2) {
            event.setCancelled(true);
        }
    }
}
