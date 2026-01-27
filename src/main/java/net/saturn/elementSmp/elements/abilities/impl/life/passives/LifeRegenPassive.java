package net.saturn.elementSmp.elements.abilities.impl.life.passives;

import net.saturn.elementSmp.elements.ElementType;
import net.saturn.elementSmp.managers.ElementManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;

/**
 * Handles Life element passive 1: Increased health regeneration
 */
public class LifeRegenPassive implements Listener {
    private final ElementManager elementManager;

    public LifeRegenPassive(ElementManager elementManager) {
        this.elementManager = elementManager;
    }

    @EventHandler
    public void onRegen(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (elementManager.getPlayerElement(player) != ElementType.LIFE) return;

        // 50% increase in regeneration amount
        event.setAmount(event.getAmount() * 1.5);
    }
}
