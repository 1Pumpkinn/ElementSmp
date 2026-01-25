package net.saturn.elementSmp.elements.abilities;

import net.saturn.elementSmp.elements.ElementContext;
import org.bukkit.entity.Player;

/**
 * Interface for all element abilities
 */
public interface Ability {
    /**
     * Execute the ability
     * @param context The context for the ability execution
     * @return true if the ability was executed successfully, false otherwise
     */
    boolean execute(ElementContext context);
    
    /**
     * Set the active state for this ability
     * @param player The player
     * @param active Whether the ability is active
     */
    void setActive(Player player, boolean active);
}
